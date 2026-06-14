package br.com.baba.eventHub.contracts;

import java.util.UUID;

public record TicketPurchaseDTO(UUID ticketID, UUID paymentID, String cardToken, Integer installments, Integer price) {
}
