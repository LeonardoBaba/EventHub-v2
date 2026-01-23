package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.dto.UserFormDTO;
import br.com.baba.eventHub.core.dto.UserResponseDTO;
import br.com.baba.eventHub.core.exceptions.UserException;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.UserRepository;
import br.com.baba.eventHub.core.security.configuration.SecurityConfigurations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityConfigurations securityConfigurations;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserFormDTO userFormDTO;

    @Mock
    private User user;

    @Test
    @DisplayName("Should create user successfully with encoded password")
    void shouldCreateUserSuccessfully() throws UserException {
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";

        when(userFormDTO.password()).thenReturn(rawPassword);

        when(securityConfigurations.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        User createdUser = userService.createUser(userFormDTO);

        assertNotNull(createdUser);

        verify(passwordEncoder).encode(rawPassword);

        verify(userRepository, times(1)).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("Should return UserResponseDTO when user is found by CPF")
    void shouldReturnUserResponseDTOWhenUserFoundByCPF() throws UserException {
        String cpf = "123.456.789-00";

        when(userRepository.findByCpf(cpf)).thenReturn(user);

        UserResponseDTO response = userService.getUserByCPF(cpf);

        assertNotNull(response);

        verify(userRepository).findByCpf(cpf);
    }
}