package br.com.baba.eventHub.core.repository;

import br.com.baba.eventHub.core.model.Ticket;
import br.com.baba.eventHub.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    long countByEventId(UUID eventId);

    boolean existsByUserAndEventId(User user, UUID eventId);
}
