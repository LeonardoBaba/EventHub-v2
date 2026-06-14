package br.com.baba.eventHub.core.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class RabbitInfraConfig {

    public static final String EXCHANGE_NAME = "eventhub.exchange";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
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
}
