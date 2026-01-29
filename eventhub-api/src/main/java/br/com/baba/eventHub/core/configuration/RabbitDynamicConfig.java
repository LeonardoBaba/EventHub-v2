package br.com.baba.eventHub.core.configuration;

import br.com.baba.eventHub.core.interfaces.IMessageReceive;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitDynamicConfig {

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                                    RabbitReceiver receiver,
                                                    List<IMessageReceive<?>> services) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        String[] queues = services.stream()
                .map(IMessageReceive::getQueue)
                .toArray(String[]::new);

        if (queues.length > 0) {
            container.setQueueNames(queues);
        }

        container.setMessageListener(receiver);

        container.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return container;
    }
}