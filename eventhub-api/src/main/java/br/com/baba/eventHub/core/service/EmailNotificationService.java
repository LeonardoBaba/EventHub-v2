package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.interfaces.IEmail;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationService implements IEmail {
    public void send(String recipient, String subject, String message) {
        System.out.println("--------------------------------------------------");
        System.out.println("📧 SIMULATING EMAIL SENDING");
        System.out.println("To: " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        System.out.println("--------------------------------------------------");
    }
}
