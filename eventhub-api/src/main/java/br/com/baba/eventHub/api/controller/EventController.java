package br.com.baba.eventHub.api.controller;

import br.com.baba.eventHub.core.dto.EventFormDTO;
import br.com.baba.eventHub.core.dto.EventResponseDTO;
import br.com.baba.eventHub.core.exceptions.EventException;
import br.com.baba.eventHub.core.model.Event;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    @SecurityRequirement(name = "bearer-key")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity createEvent(@Valid @RequestBody EventFormDTO eventFormDTO, Authentication authentication) {
        User orgazinUser = (User) authentication.getPrincipal();
        Event event = eventService.createEvent(eventFormDTO, orgazinUser);
        return ResponseEntity.ok(new EventResponseDTO(event));
    }

    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        Page<Event> eventsPage = eventService.getAllActiveEvents(pageable, startDate, endDate);
        return ResponseEntity.ok(eventsPage.map(EventResponseDTO::new));
    }

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "bearer-key")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity cancelEvent(@PathVariable UUID id) throws EventException {
        eventService.cancelEvent(id);
        return ResponseEntity.ok().build();
    }
}
