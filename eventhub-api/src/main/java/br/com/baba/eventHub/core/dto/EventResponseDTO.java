package br.com.baba.eventHub.core.dto;

import br.com.baba.eventHub.core.enums.EventStatusEnum;
import br.com.baba.eventHub.core.model.Event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponseDTO(UUID id, String title, String description, LocalDateTime date, String location,
                               Integer capacity,
                               EventStatusEnum status) {
    public EventResponseDTO(Event event) {
        this(event.getId(), event.getTitle(), event.getDescription(), event.getDate(), event.getLocation(), event.getCapacity(), event.getStatusEnum());
    }
}
