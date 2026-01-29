package br.com.baba.eventHub.core.configuration;

import br.com.baba.eventHub.core.interfaces.IMessageReceive;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitAutoDeclarer {

    @Bean
    public Declarables createRabbitMQScheme(List<IMessageReceive<?>> services) {

        List<Declarable> declarables = new ArrayList<>();

        DirectExchange defaultExchange = new DirectExchange(RabbitInfraConfig.EXCHANGE_NAME);
        declarables.add(defaultExchange);

        for (IMessageReceive<?> service : services) {
            Queue queue = new Queue(service.getQueue(), true);

            Binding binding = BindingBuilder.bind(queue)
                    .to(defaultExchange)
                    .with(service.getRoutingKey());

            declarables.add(queue);
            declarables.add(binding);
        }

        return new Declarables(declarables);
    }
}
