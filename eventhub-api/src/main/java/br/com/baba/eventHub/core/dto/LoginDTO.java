package br.com.baba.eventHub.core.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(@NotBlank String name, @NotBlank String password) {
}