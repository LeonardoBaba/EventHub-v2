package br.com.baba.eventHub.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UserFormDTO(

        @NotNull
        String name,

        @NotNull
        @Email
        String email,

        @NotNull
        @CPF
        @Pattern(regexp = "\\d{11}")
        String cpf,

        @NotNull
        @Size(min = 8, max = 72)
        String password

) {
}
