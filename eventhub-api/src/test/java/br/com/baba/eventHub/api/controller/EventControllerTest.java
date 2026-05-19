package br.com.baba.eventHub.api.controller;

import br.com.baba.eventHub.core.dto.EventFormDTO;
import br.com.baba.eventHub.core.dto.EventResponseDTO;
import br.com.baba.eventHub.core.enums.EventStatusEnum;
import br.com.baba.eventHub.core.model.Event;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.service.EventService;
import br.com.baba.eventHub.core.service.IdempotencyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private Authentication authentication;

    @Mock
    private EventFormDTO eventFormDTO;

    @Mock
    private User user;

    @Mock
    private Event event;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should return OK when creating an event without Idempotency-Key")
    void shouldReturnOkWhenCreatingEvent() {
        when(event.getId()).thenReturn(UUID.randomUUID());
        when(event.getTitle()).thenReturn("Test Event");
        when(event.getDescription()).thenReturn("Description");
        when(event.getDate()).thenReturn(LocalDateTime.now());
        when(event.getLocation()).thenReturn("Location");
        when(event.getCapacity()).thenReturn(100);
        when(event.getPrice()).thenReturn(5000);

        when(authentication.getPrincipal()).thenReturn(user);
        when(eventService.createEvent(any(EventFormDTO.class), eq(user))).thenReturn(event);

        ResponseEntity<EventResponseDTO> response = eventController.createEvent(eventFormDTO, authentication, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        verify(eventService).createEvent(any(EventFormDTO.class), eq(user));
        verify(idempotencyService, never()).findCachedResponse(any(), any());
        verify(idempotencyService, never()).saveResponse(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when Idempotency-Key is not a valid UUID")
    void shouldReturnBadRequestWhenIdempotencyKeyIsInvalid() {
        ResponseEntity<EventResponseDTO> response = eventController.createEvent(eventFormDTO, authentication, "not-a-uuid");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(eventService, never()).createEvent(any(), any());
        verify(idempotencyService, never()).findCachedResponse(any(), any());
    }

    @Test
    @DisplayName("Should return cached response without creating event when Idempotency-Key already exists")
    void shouldReturnCachedResponseWhenIdempotencyKeyExists() {
        UUID idempotencyKey = UUID.randomUUID();

        EventResponseDTO cachedResponse = new EventResponseDTO(
                UUID.randomUUID(), "Cached Event", "Description",
                LocalDateTime.now().plusDays(1), "Location", 100, 5000, EventStatusEnum.ACTIVE
        );

        when(idempotencyService.findCachedResponse(idempotencyKey, EventResponseDTO.class)).thenReturn(Optional.of(cachedResponse));

        ResponseEntity<EventResponseDTO> response = eventController.createEvent(eventFormDTO, authentication, idempotencyKey.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedResponse, response.getBody());
        verify(eventService, never()).createEvent(any(), any());
        verify(idempotencyService, never()).saveResponse(any(), any());
    }

    @Test
    @DisplayName("Should create event and save response when Idempotency-Key is new")
    void shouldCreateEventAndCacheResponseWhenIdempotencyKeyIsNew() {
        UUID idempotencyKey = UUID.randomUUID();

        when(event.getId()).thenReturn(UUID.randomUUID());
        when(event.getTitle()).thenReturn("New Event");
        when(event.getDescription()).thenReturn("Description");
        when(event.getDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(event.getLocation()).thenReturn("Location");
        when(event.getCapacity()).thenReturn(100);
        when(event.getPrice()).thenReturn(5000);

        when(authentication.getPrincipal()).thenReturn(user);
        when(eventService.createEvent(any(EventFormDTO.class), eq(user))).thenReturn(event);
        when(idempotencyService.findCachedResponse(idempotencyKey, EventResponseDTO.class)).thenReturn(Optional.empty());
        when(idempotencyService.reserveIdempotencyKey(idempotencyKey)).thenReturn(true);

        ResponseEntity<EventResponseDTO> response = eventController.createEvent(eventFormDTO, authentication, idempotencyKey.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        verify(eventService).createEvent(any(EventFormDTO.class), eq(user));
        verify(idempotencyService).saveResponse(eq(idempotencyKey), any(EventResponseDTO.class));
    }

    @Test
    @DisplayName("Should return page of events when getting all events")
    void shouldReturnPageOfEventsWhenGettingAllEvents() {
        when(event.getId()).thenReturn(UUID.randomUUID());
        when(event.getTitle()).thenReturn("Test Event");
        when(event.getDescription()).thenReturn("Description");
        when(event.getDate()).thenReturn(LocalDateTime.now());
        when(event.getLocation()).thenReturn("Location");
        when(event.getCapacity()).thenReturn(100);
        when(event.getPrice()).thenReturn(5000);

        Page<Event> eventsPage = new PageImpl<>(Collections.singletonList(event));
        when(eventService.getAllActiveEvents(any(Pageable.class), any(), any())).thenReturn(eventsPage);

        ResponseEntity<Page<EventResponseDTO>> response = eventController.getAllEvents(Pageable.unpaged(), null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        verify(eventService).getAllActiveEvents(any(Pageable.class), any(), any());
    }

    @Test
    @DisplayName("Should return OK when cancelling an event")
    void shouldReturnOkWhenCancellingEvent() throws Exception {
        UUID eventId = UUID.randomUUID();

        ResponseEntity<Void> response = eventController.cancelEvent(eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(eventService).cancelEvent(eventId);
    }
}
