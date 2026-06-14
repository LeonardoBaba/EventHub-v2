package br.com.baba.eventHub.core.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TicketFormDTO(
        @NotNull
        String cardToken,

        @NotNull
        @Min(1)
        @Max(12)
        Integer installments
) {
}
