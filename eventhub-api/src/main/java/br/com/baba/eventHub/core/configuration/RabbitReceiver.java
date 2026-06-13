package br.com.baba.eventHub.core.configuration;

import br.com.baba.eventHub.core.interfaces.IMessageReceive;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RabbitReceiver implements MessageListener {

    private final Map<String, IMessageReceive<?>> strategies = new HashMap<>();

    private final ObjectMapper objectMapper;

    public RabbitReceiver(List<IMessageReceive<?>> services, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        for (IMessageReceive<?> service : services) {
            this.strategies.put(service.getQueue(), service);
        }
    }

    @Override
    public void onMessage(Message message) {
        String jsonBody = new String(message.getBody(), StandardCharsets.UTF_8);
        String originQueue = message.getMessageProperties().getConsumerQueue();
        IMessageReceive<?> strategy = strategies.get(originQueue);

        if (strategy == null) {
            log.error("No strategy registered for queue '{}' - routing to DLQ", originQueue);
            throw new AmqpRejectAndDontRequeueException("No strategy registered for queue: " + originQueue);
        }

        processMessage(strategy, jsonBody);
    }

    private <T> void processMessage(IMessageReceive<T> strategy, String json) {
        T mappedObject;
        try {
            mappedObject = objectMapper.readValue(json, strategy.getPayloadType());
        } catch (IOException e) {
            log.error("Invalid payload on queue '{}' - routing to DLQ", strategy.getQueue(), e);
            throw new AmqpRejectAndDontRequeueException("Invalid payload on queue " + strategy.getQueue(), e);
        }
        strategy.processMessage(mappedObject);
    }
}
