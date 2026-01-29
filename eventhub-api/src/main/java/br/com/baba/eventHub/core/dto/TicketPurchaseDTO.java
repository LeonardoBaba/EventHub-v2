package br.com.baba.eventHub.core.dto;

import br.com.baba.eventHub.core.model.Payment;

import java.util.UUID;

public record TicketPurchaseDTO(UUID ticketID, String cardToken, Integer installments) {
    public TicketPurchaseDTO(Payment payment) {
        this(payment.getTicket().getId(), payment.getCardToken(), payment.getInstallments());
    }
}

