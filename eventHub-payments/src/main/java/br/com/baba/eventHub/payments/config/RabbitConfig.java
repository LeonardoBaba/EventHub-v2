package br.com.baba.eventHub.payments.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@Configuration
public class RabbitConfig {

    @Value("${mq.exchange.name}")
    private String exchangeName;

    @Value("${mq.queue.input}")
    private String inputQueueName;

    @Value("${mq.routing.key.input}")
    private String inputRoutingKey;

    @Value("${mq.dlx.name}")
    private String dlxName;

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxName);
    }

    @Bean
    public Queue inputQueue() {
        return QueueBuilder.durable(inputQueueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", inputQueueName + ".dlq")
                .build();
    }

    @Bean
    public Binding binding(Queue inputQueue, DirectExchange exchange) {
        return BindingBuilder.bind(inputQueue).to(exchange).with(inputRoutingKey);
    }

    @Bean
    public Queue inputDeadLetterQueue() {
        return QueueBuilder.durable(inputQueueName + ".dlq").build();
    }

    @Bean
    public Binding inputDeadLetterBinding(Queue inputDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(inputDeadLetterQueue).to(deadLetterExchange).with(inputQueueName + ".dlq");
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setTaskExecutor(new VirtualThreadTaskExecutor("rabbit-vt-"));
        factory.setConcurrentConsumers(50);
        factory.setMaxConcurrentConsumers(200);
        factory.setPrefetchCount(1);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
