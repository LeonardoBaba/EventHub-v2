package br.com.baba.eventHub.core.interfaces;

import br.com.baba.eventHub.core.enums.RoutingKeyEnum;

public interface IMessage {
    void send(Object message, RoutingKeyEnum routingKey);
}
