package br.com.baba.eventHub.core.model;

import br.com.baba.eventHub.core.dto.EventFormDTO;
import br.com.baba.eventHub.core.enums.EventStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatusEnum statusEnum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    public Event(EventFormDTO eventFormDTO, User organizer) {
        this.title = eventFormDTO.title();
        this.description = eventFormDTO.description();
        this.date = eventFormDTO.date();
        this.location = eventFormDTO.location();
        this.capacity = eventFormDTO.capacity();
        this.statusEnum = EventStatusEnum.ACTIVE;
        this.organizer = organizer;
    }
}
