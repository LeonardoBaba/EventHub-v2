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

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class RabbitConfig {

    @Value("${mq.exchange.name}")
    private String exchangeName;

    @Value("${mq.queue.input}")
    private String inputQueueName;

    @Value("${mq.routing.key.input}")
    private String inputRoutingKey;

    @Value("${mq.queue.output}")
    private String outputQueueName;

    @Value("${mq.routing.key.output}")
    private String outputRoutingKey;

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
        template.setMandatory(true);
        template.setConfirmCallback((correlation, ack, cause) -> {
            if (!ack) {
                log.error("Publish nacked by broker: cause={} correlation={}", cause, correlation);
            }
        });
        template.setReturnsCallback(returned ->
                log.error("Message returned (unroutable): exchange={} routingKey={} replyText={} body={}",
                        returned.getExchange(), returned.getRoutingKey(), returned.getReplyText(),
                        new String(returned.getMessage().getBody(), StandardCharsets.UTF_8)));
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
    public Queue outputQueue() {
        return QueueBuilder.durable(outputQueueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", outputQueueName + ".dlq")
                .build();
    }

    @Bean
    public Binding outputBinding(Queue outputQueue, DirectExchange exchange) {
        return BindingBuilder.bind(outputQueue).to(exchange).with(outputRoutingKey);
    }

    @Bean
    public Queue outputDeadLetterQueue() {
        return QueueBuilder.durable(outputQueueName + ".dlq").build();
    }

    @Bean
    public Binding outputDeadLetterBinding(Queue outputDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(outputDeadLetterQueue).to(deadLetterExchange).with(outputQueueName + ".dlq");
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
