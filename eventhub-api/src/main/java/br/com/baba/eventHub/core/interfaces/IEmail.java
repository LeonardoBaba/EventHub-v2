package br.com.baba.eventHub.core.interfaces;

public interface IEmail {
    void send(String recipient, String subject, String message);
}
