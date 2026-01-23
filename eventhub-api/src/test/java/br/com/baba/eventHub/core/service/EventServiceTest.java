package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.dto.EventFormDTO;
import br.com.baba.eventHub.core.enums.EventStatusEnum;
import br.com.baba.eventHub.core.exceptions.EventException;
import br.com.baba.eventHub.core.interfaces.IEmail;
import br.com.baba.eventHub.core.model.Event;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketService ticketService;

    @Mock
    private IEmail email;

    @Mock
    private EventFormDTO eventFormDTO;

    @Mock
    private User organizer;

    @Mock
    private Event event;

    @Test
    @DisplayName("Should create event successfully")
    void shouldCreateEventSuccessfully() {

        eventService.createEvent(eventFormDTO, organizer);

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Should cancel event successfully when event is active")
    void shouldCancelEventSuccessfully() throws EventException {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.ACTIVE);

        eventService.cancelEvent(eventId);

        verify(event).setStatusEnum(EventStatusEnum.CANCELLED);
    }

    @Test
    @DisplayName("Should throw EventException when trying to cancel an already cancelled event")
    void shouldThrowExceptionWhenCancellingCancelledEvent() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.CANCELLED);

        assertThrows(EventException.class, () -> eventService.cancelEvent(eventId));
        verify(event, never()).setStatusEnum(any());
    }

    @Test
    @DisplayName("Should send warning email when attendance is low")
    void shouldSendWarningWhenAttendanceIsLow() {
        UUID eventId = UUID.randomUUID();

        when(event.getId()).thenReturn(eventId);
        when(event.getCapacity()).thenReturn(100);
        when(event.getTitle()).thenReturn("Test Event");
        when(event.getDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(event.getOrganizer()).thenReturn(organizer);
        when(organizer.getEmail()).thenReturn("organizer@test.com");

        when(eventRepository.findAllByStatusEnumAndDateLessThanEqual(any(), any()))
                .thenReturn(List.of(event));

        when(ticketService.getTicketCountPerEvent(eventId)).thenReturn(10);

        eventService.checkLowAttendanceWarning();

        verify(email).send(eq("organizer@test.com"), eq("Low Attendance Warning"), anyString());
    }

    @Test
    @DisplayName("Should not send warning email when attendance is sufficient")
    void shouldNotSendWarningWhenAttendanceIsSufficient() {
        UUID eventId = UUID.randomUUID();

        when(event.getId()).thenReturn(eventId);
        when(event.getCapacity()).thenReturn(100);

        when(eventRepository.findAllByStatusEnumAndDateLessThanEqual(any(), any()))
                .thenReturn(List.of(event));

        when(ticketService.getTicketCountPerEvent(eventId)).thenReturn(30);

        eventService.checkLowAttendanceWarning();

        verify(email, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when checking status for purchase and event is full")
    void shouldThrowExceptionWhenCheckingStatusAndEventIsFull() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.ACTIVE);
        when(event.getCapacity()).thenReturn(100);

        assertThrows(EventException.class, () -> eventService.checkEventStatusForPurchase(eventId, 100));
    }

    @Test
    @DisplayName("Should check event status for purchase successfully")
    void shouldCheckEventStatusForPurchaseSuccessfully() throws EventException {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(event.getStatusEnum()).thenReturn(EventStatusEnum.ACTIVE);
        when(event.getCapacity()).thenReturn(100);

        eventService.checkEventStatusForPurchase(eventId, 99);

        verify(eventRepository).findById(eventId);
    }
}
