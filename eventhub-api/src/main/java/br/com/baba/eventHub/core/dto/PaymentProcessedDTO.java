package br.com.baba.eventHub.core.dto;

import br.com.baba.eventHub.core.enums.PaymentStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentProcessedDTO(UUID ticketID, UUID paymentID, PaymentStatusEnum status, LocalDateTime date) {
    public String toString() {
        return "PaymentProcessedDTO{" +
                "ticketID=" + ticketID +
                ", paymentID=" + paymentID +
                ", status=" + status +
                ", date=" + date +
                '}';
    }
}
