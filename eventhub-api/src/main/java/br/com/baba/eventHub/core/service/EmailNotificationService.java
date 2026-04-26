package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.interfaces.IEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationService implements IEmail {
    public void send(String recipient, String subject, String message) {
        log.info("📧 SIMULATING EMAIL SENDING — to={} subject=\"{}\" message=\"{}\"",
                recipient, subject, message);
    }
}
