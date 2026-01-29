package br.com.baba.eventHub.core.enums;

public enum RabbitQueueEnum {

    QUEUE_PAYMENT_PROCESSED("eventhub.payment.processed");


    private final String name;

    RabbitQueueEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
