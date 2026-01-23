package br.com.baba.eventHub.api.controller;

import br.com.baba.eventHub.core.dto.EventFormDTO;
import br.com.baba.eventHub.core.dto.EventResponseDTO;
import br.com.baba.eventHub.core.model.Event;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.service.EventService;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

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
    @DisplayName("Should return OK when creating an event")
    void shouldReturnOkWhenCreatingEvent() {
        when(event.getId()).thenReturn(UUID.randomUUID());
        when(event.getTitle()).thenReturn("Test Event");
        when(event.getDescription()).thenReturn("Description");
        when(event.getDate()).thenReturn(LocalDateTime.now());
        when(event.getLocation()).thenReturn("Location");
        when(event.getCapacity()).thenReturn(100);

        when(authentication.getPrincipal()).thenReturn(user);
        when(eventService.createEvent(any(EventFormDTO.class), eq(user))).thenReturn(event);

        ResponseEntity response = eventController.createEvent(eventFormDTO, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        verify(eventService).createEvent(any(EventFormDTO.class), eq(user));
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

        ResponseEntity response = eventController.cancelEvent(eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(eventService).cancelEvent(eventId);
    }
}