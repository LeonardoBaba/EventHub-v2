package br.com.baba.eventHub.core.repository;

import br.com.baba.eventHub.core.model.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, UUID> {

    Optional<IdempotencyRecord> findByIdempotencyKeyAndExpiresAtAfter(UUID idempotencyKey, LocalDateTime now);

    Optional<IdempotencyRecord> findByIdempotencyKey(UUID idempotencyKey);

    @Modifying
    @Query("DELETE FROM IdempotencyRecord i WHERE i.expiresAt < :date")
    void deleteByExpiresAtBefore(@Param("date") LocalDateTime date);
}
