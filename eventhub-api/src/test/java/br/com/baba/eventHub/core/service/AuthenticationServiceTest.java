package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User userMock;

    @Test
    @DisplayName("Should load user details successfully when username exists")
    void shouldLoadUserByUsernameSuccessfully() {
        String username = "john";

        when(userRepository.findByName(username)).thenReturn(userMock);
        UserDetails userDetails = authenticationService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(userMock, userDetails);
        verify(userRepository).findByName(username);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when username does not exist")
    void shouldThrowExceptionWhenUserNotFound() {
        String username = "unknown";
        when(userRepository.findByName(username)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.loadUserByUsername(username));
        verify(userRepository).findByName(username);
    }
}