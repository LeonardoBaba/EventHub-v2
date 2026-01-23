package br.com.baba.eventHub.api.controller;

import br.com.baba.eventHub.core.dto.UserFormDTO;
import br.com.baba.eventHub.core.dto.UserResponseDTO;
import br.com.baba.eventHub.core.exceptions.UserException;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private UserFormDTO userFormDTO;

    @Mock
    private User user;

    @Mock
    private UserResponseDTO userResponseDTO;

    @Test
    @DisplayName("Should return OK when creating a user")
    void shouldReturnOkWhenCreatingUser() throws UserException {
        when(userService.createUser(any(UserFormDTO.class))).thenReturn(user);

        ResponseEntity response = userController.createUser(userFormDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).createUser(any(UserFormDTO.class));
    }

    @Test
    @DisplayName("Should return OK and UserResponseDTO when getting user by CPF")
    void shouldReturnOkAndUserResponseDtoWhenGettingUserByCpf() throws UserException {
        String cpf = "12345678901";
        when(userService.getUserByCPF(cpf)).thenReturn(userResponseDTO);

        ResponseEntity response = userController.getUser(cpf);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponseDTO, response.getBody());
        verify(userService).getUserByCPF(cpf);
    }
}
