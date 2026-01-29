package br.com.baba.eventHub.core.interfaces;

public interface IMessageReceive<T> {

    String getQueue();

    Class<T> getPayloadType();

    void processMessage(T payload);

    String getRoutingKey();
}