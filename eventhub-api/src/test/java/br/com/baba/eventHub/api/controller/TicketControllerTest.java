package br.com.baba.eventHub.api.controller;

import br.com.baba.eventHub.core.dto.TicketFormDTO;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @InjectMocks
    private TicketController ticketController;

    @Mock
    private TicketService ticketService;

    @Mock
    private Authentication authentication;

    @Mock
    private User user;

    @Test
    @DisplayName("Should return Accepted when purchasing a ticket")
    void shouldReturnAcceptedWhenPurchasingTicket() throws Exception {
        UUID eventId = UUID.randomUUID();
        TicketFormDTO ticketFormDTO = new TicketFormDTO("123", 1);
        when(authentication.getPrincipal()).thenReturn(user);

        ResponseEntity response = ticketController.purchaseTicket(eventId, ticketFormDTO, authentication);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(ticketService).purchaseTicket(eventId, user, ticketFormDTO);
    }
}
