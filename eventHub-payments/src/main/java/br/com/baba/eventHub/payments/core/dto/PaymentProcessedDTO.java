package br.com.baba.eventHub.payments.core.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentProcessedDTO(UUID ticketID, UUID paymentID, String status, LocalDateTime processedDate) {
}