package br.com.baba.eventHub.payments.core.dto;

import br.com.baba.eventHub.payments.core.enums.PaymentStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentProcessedDTO(UUID ticketID, UUID paymentID, PaymentStatusEnum status, LocalDateTime processedDate) {
}
