package br.com.baba.eventHub.core.dto;

import jakarta.validation.constraints.NotNull;

public record TicketFormDTO(@NotNull String cardToken, @NotNull Integer installments) {
}
