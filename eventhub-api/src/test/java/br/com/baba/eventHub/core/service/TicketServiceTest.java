package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.enums.EventStatusEnum;
import br.com.baba.eventHub.core.exceptions.EventException;
import br.com.baba.eventHub.core.interfaces.IEmail;
import br.com.baba.eventHub.core.model.Event;
import br.com.baba.eventHub.core.model.Ticket;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.EventRepository;
import br.com.baba.eventHub.core.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private IEmail email;

    @Mock
    private User user;

    @Mock
    private User organizer;

    @Mock
    private Event event;

    @Test
    @DisplayName("Should purchase ticket successfully")
    void shouldPurchaseTicketSuccessfully() throws EventException {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.ACTIVE);
        when(event.getCapacity()).thenReturn(100);
        when(event.getTitle()).thenReturn("Awesome Event");

        when(user.getEmail()).thenReturn("user@test.com");

        when(ticketRepository.existsByUserAndEventId(user, eventId)).thenReturn(false);
        when(ticketRepository.countByEventId(eventId)).thenReturn(1L);

        ticketService.purchaseTicket(eventId, user);

        verify(ticketRepository).save(any(Ticket.class));
        verify(email).send(eq("user@test.com"), contains("Ticket Purchased"), anyString());
    }

    @Test
    @DisplayName("Should throw EventException when user already purchased ticket")
    void shouldThrowExceptionWhenUserAlreadyPurchased() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.ACTIVE);

        when(ticketRepository.existsByUserAndEventId(user, eventId)).thenReturn(true);

        assertThrows(EventException.class, () -> ticketService.purchaseTicket(eventId, user));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should notify organizer when event is sold out")
    void shouldNotifyOrganizerWhenEventIsSoldOut() throws EventException {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.ACTIVE);
        when(event.getId()).thenReturn(eventId);
        when(event.getTitle()).thenReturn("Sold Out Event");
        when(event.getCapacity()).thenReturn(10);
        when(event.getOrganizer()).thenReturn(organizer);
        when(organizer.getEmail()).thenReturn("organizer@test.com");

        when(user.getEmail()).thenReturn("user@test.com");

        when(ticketRepository.existsByUserAndEventId(user, eventId)).thenReturn(false);
        when(ticketRepository.countByEventId(eventId)).thenReturn(9L, 10L);

        ticketService.purchaseTicket(eventId, user);

        verify(ticketRepository).save(any(Ticket.class));
        verify(email).send(eq("organizer@test.com"), contains("SOLD OUT"), anyString());
    }
}
