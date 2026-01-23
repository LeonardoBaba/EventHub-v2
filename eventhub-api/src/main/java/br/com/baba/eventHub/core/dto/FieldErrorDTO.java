package br.com.baba.eventHub.core.dto;

import org.springframework.validation.FieldError;

public record FieldErrorDTO(String field, String message) {
    public FieldErrorDTO(FieldError error) {
        this(error.getField(), error.getDefaultMessage());
    }
}
