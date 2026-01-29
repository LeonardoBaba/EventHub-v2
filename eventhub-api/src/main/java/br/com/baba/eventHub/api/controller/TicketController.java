package br.com.baba.eventHub.api.controller;

import br.com.baba.eventHub.core.dto.TicketFormDTO;
import br.com.baba.eventHub.core.exceptions.EventException;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/api/events/{eventId}/tickets")
    public ResponseEntity purchaseTicket(@PathVariable("eventId") UUID eventId, @Valid @RequestBody TicketFormDTO ticketFormDTO, Authentication authentication) throws EventException {
        User user = (User) authentication.getPrincipal();
        ticketService.purchaseTicket(eventId, user, ticketFormDTO);
        return ResponseEntity.accepted().build();
    }
}
