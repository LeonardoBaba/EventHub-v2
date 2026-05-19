package br.com.baba.eventHub.api.controller;

import br.com.baba.eventHub.core.dto.EventFormDTO;
import br.com.baba.eventHub.core.dto.EventResponseDTO;
import br.com.baba.eventHub.core.exceptions.EventException;
import br.com.baba.eventHub.core.exceptions.IdempotencyConflictException;
import br.com.baba.eventHub.core.model.Event;
import br.com.baba.eventHub.core.model.User;
import br.com.baba.eventHub.core.service.EventService;
import br.com.baba.eventHub.core.service.IdempotencyService;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private IdempotencyService idempotencyService;

    @PostMapping
    @SecurityRequirement(name = "bearer-key")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventFormDTO eventFormDTO,
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader) {

        UUID idempotencyKey = null;
        if (idempotencyKeyHeader != null) {
            try {
                idempotencyKey = UUID.fromString(idempotencyKeyHeader);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        if (idempotencyKey != null) {
            Optional<EventResponseDTO> cached = idempotencyService.findCachedResponse(idempotencyKey, EventResponseDTO.class);
            if (cached.isPresent()) {
                return ResponseEntity.ok(cached.get());
            }

            boolean reserved = idempotencyService.reserveIdempotencyKey(idempotencyKey);
            if (!reserved) {
                throw new IdempotencyConflictException("Request already being processed or already completed");
            }
        }

        User organizer = (User) authentication.getPrincipal();
        Event event;
        try {
            event = eventService.createEvent(eventFormDTO, organizer);
        } catch (Exception e) {
            if (idempotencyKey != null) {
                idempotencyService.releaseKey(idempotencyKey);
            }
            throw e;
        }

        EventResponseDTO responseDTO = new EventResponseDTO(event);

        if (idempotencyKey != null) {
            idempotencyService.saveResponse(idempotencyKey, responseDTO);
        }

        return ResponseEntity.ok(responseDTO);
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
    public ResponseEntity<Void> cancelEvent(@PathVariable UUID id) throws EventException {
        eventService.cancelEvent(id);
        return ResponseEntity.ok().build();
    }
}
