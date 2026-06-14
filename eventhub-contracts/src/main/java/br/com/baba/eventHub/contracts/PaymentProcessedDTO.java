package br.com.baba.eventHub.contracts;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentProcessedDTO(UUID ticketID, UUID paymentID, PaymentStatusEnum status, LocalDateTime processedDate) {
}
