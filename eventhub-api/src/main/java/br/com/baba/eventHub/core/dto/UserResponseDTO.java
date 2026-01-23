package br.com.baba.eventHub.core.dto;

import br.com.baba.eventHub.core.enums.UserRoleEnum;
import br.com.baba.eventHub.core.model.User;

public record UserResponseDTO(String name, String email, UserRoleEnum role) {

    public UserResponseDTO(User user) {
        this(user.getName(), user.getEmail(), user.getRole());
    }
}
