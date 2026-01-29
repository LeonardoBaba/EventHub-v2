package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.configuration.RabbitInfraConfig;
import br.com.baba.eventHub.core.enums.RoutingKeyEnum;
import br.com.baba.eventHub.core.interfaces.IMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQService implements IMessage {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void send(Object message, RoutingKeyEnum routingKey) {
        rabbitTemplate.convertAndSend(
                RabbitInfraConfig.EXCHANGE_NAME,
                routingKey.getRoutingName(),
                message
        );
    }
}
