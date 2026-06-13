package br.com.baba.eventHub.core.configuration;

import br.com.baba.eventHub.core.interfaces.IMessageReceive;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitAutoDeclarer {

    @Value("${mq.dlx.name}")
    private String dlxName;

    @Bean
    public Declarables createRabbitMQScheme(List<IMessageReceive<?>> services) {

        List<Declarable> declarables = new ArrayList<>();

        DirectExchange defaultExchange = new DirectExchange(RabbitInfraConfig.EXCHANGE_NAME);
        DirectExchange deadLetterExchange = new DirectExchange(dlxName);
        declarables.add(defaultExchange);
        declarables.add(deadLetterExchange);

        for (IMessageReceive<?> service : services) {
            String queueName = service.getQueue();
            String dlqName = queueName + ".dlq";

            Queue queue = QueueBuilder.durable(queueName)
                    .withArgument("x-dead-letter-exchange", dlxName)
                    .withArgument("x-dead-letter-routing-key", dlqName)
                    .build();

            Binding binding = BindingBuilder.bind(queue)
                    .to(defaultExchange)
                    .with(service.getRoutingKey());

            Queue dlq = QueueBuilder.durable(dlqName).build();
            Binding dlqBinding = BindingBuilder.bind(dlq).to(deadLetterExchange).with(dlqName);

            declarables.add(queue);
            declarables.add(binding);
            declarables.add(dlq);
            declarables.add(dlqBinding);
        }

        return new Declarables(declarables);
    }
}
