package br.com.baba.eventHub.core.configuration;

import br.com.baba.eventHub.core.interfaces.IMessageReceive;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try {
            String jsonBody = new String(message.getBody(), StandardCharsets.UTF_8);

            String originQueue = message.getMessageProperties().getConsumerQueue();
            IMessageReceive<?> strategy = strategies.get(originQueue);

            if (strategy == null) {
                System.err.println("Service not found for queue: " + originQueue);
                return;
            }

            processMessage(strategy, jsonBody);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private <T> void processMessage(IMessageReceive<T> strategy, String json) throws IOException {
        T mappedObject = objectMapper.readValue(json, strategy.getPayloadType());

        strategy.processMessage(mappedObject);
    }
}
