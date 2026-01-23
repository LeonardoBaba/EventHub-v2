package br.com.baba.eventHub.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserFormDTO(

        @NotNull
        String name,

        @NotNull
        @Email
        String email,

        @NotNull
        @Pattern(regexp = "\\d+")
        @Size(min = 11, max = 14)
        String cpf,

        @NotNull
        String password

) {
}
