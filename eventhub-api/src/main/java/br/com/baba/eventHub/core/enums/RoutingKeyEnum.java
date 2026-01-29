package br.com.baba.eventHub.core.enums;

public enum RoutingKeyEnum {

    PAYMENT_CREATED("payment.created"),

    PAYMENT_PROCESSED("payment.processed");

    final String name;

    RoutingKeyEnum(String name) {
        this.name = name;
    }

    public String getRoutingName() {
        return name;
    }

}
