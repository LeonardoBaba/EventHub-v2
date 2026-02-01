package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.dto.TicketFormDTO;
import br.com.baba.eventHub.core.enums.EventStatusEnum;
import br.com.baba.eventHub.core.enums.TicketStatusEnum;
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

import java.util.List;
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
    private PaymentService paymentService;

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
        TicketFormDTO ticketFormDTO = new TicketFormDTO("123", 1);

        when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.ACTIVE);
        when(event.getCapacity()).thenReturn(100);

        when(ticketRepository.existsByUserAndEventId(user, eventId)).thenReturn(false);
        when(ticketRepository.countByEventIdAndStatusIn(eventId, List.of(TicketStatusEnum.CONFIRMED, TicketStatusEnum.PENDING))).thenReturn(1L);

        ticketService.purchaseTicket(eventId, user, ticketFormDTO);

        verify(ticketRepository).save(any(Ticket.class));
        verify(paymentService).processPayment(any(Ticket.class), eq(ticketFormDTO));
    }

    @Test
    @DisplayName("Should throw EventException when user already purchased ticket")
    void shouldThrowExceptionWhenUserAlreadyPurchased() {
        UUID eventId = UUID.randomUUID();
        TicketFormDTO ticketFormDTO = new TicketFormDTO("123", 1);

        when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.ACTIVE);

        when(ticketRepository.existsByUserAndEventId(user, eventId)).thenReturn(true);

        assertThrows(EventException.class, () -> ticketService.purchaseTicket(eventId, user, ticketFormDTO));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should notify organizer when event is sold out")
    void shouldNotifyOrganizerWhenEventIsSoldOut() throws EventException {
        UUID eventId = UUID.randomUUID();
        TicketFormDTO ticketFormDTO = new TicketFormDTO("123", 1);

        when(eventRepository.findByIdWithLock(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.ACTIVE);
        when(event.getId()).thenReturn(eventId);
        when(event.getTitle()).thenReturn("Sold Out Event");
        when(event.getCapacity()).thenReturn(10);
        when(event.getOrganizer()).thenReturn(organizer);
        when(organizer.getEmail()).thenReturn("organizer@test.com");

        when(ticketRepository.existsByUserAndEventId(user, eventId)).thenReturn(false);
        when(ticketRepository.countByEventIdAndStatusIn(eventId, List.of(TicketStatusEnum.CONFIRMED, TicketStatusEnum.PENDING))).thenReturn(9L, 10L);

        ticketService.purchaseTicket(eventId, user, ticketFormDTO);

        verify(ticketRepository).save(any(Ticket.class));
        verify(paymentService).processPayment(any(Ticket.class), eq(ticketFormDTO));
        verify(email).send(eq("organizer@test.com"), contains("SOLD OUT"), anyString());
    }
}
