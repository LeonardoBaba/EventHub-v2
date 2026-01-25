package br.com.baba.eventHub.core.model;

import br.com.baba.eventHub.core.enums.TicketStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "tickets")
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ticket_date", nullable = false)
    private LocalDateTime ticketDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TicketStatusEnum statusEnum;

    public Ticket(User user, Event event) {
        this.ticketDate = LocalDateTime.now();
        this.user = user;
        this.event = event;
        this.statusEnum = TicketStatusEnum.PENDING;
    }
}
