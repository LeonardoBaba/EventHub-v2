package br.com.baba.eventHub.core.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventFormDTO(
        @NotNull
        String title,

        String description,

        @NotNull
        LocalDateTime date,

        @NotNull
        String location,

        @NotNull
        @Min(1)
        Integer capacity) {
}
