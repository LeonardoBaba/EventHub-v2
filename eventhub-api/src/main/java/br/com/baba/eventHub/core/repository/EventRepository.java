package br.com.baba.eventHub.core.repository;

import br.com.baba.eventHub.core.enums.EventStatusEnum;
import br.com.baba.eventHub.core.model.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e FROM Event e WHERE e.statusEnum = :status " +
            "AND (cast(:startDate as timestamp) IS NULL OR e.date >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR e.date <= :endDate)")
    Page<Event> findActiveEventsWithFilters(
            @Param("status") EventStatusEnum status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    List<Event> findAllByStatusEnumAndDateLessThanEqual(EventStatusEnum active, LocalDateTime eventDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithLock(@Param("id") UUID id);
}
