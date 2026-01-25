package br.com.baba.eventHub.core.repository;

import br.com.baba.eventHub.core.enums.TicketStatusEnum;
import br.com.baba.eventHub.core.model.Ticket;
import br.com.baba.eventHub.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.statusEnum IN :statuses")
    long countByEventIdAndStatusIn(@Param("eventId") UUID eventId, @Param("statuses") List<TicketStatusEnum> statuses);

    boolean existsByUserAndEventId(User user, UUID eventId);
}
