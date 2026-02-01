package br.com.baba.eventHub.payments.core.dto;

import java.util.UUID;

public record TicketPurchaseDTO(UUID ticketID, UUID paymentID, String cardToken, Integer installments) {
}