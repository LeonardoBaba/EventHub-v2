package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.model.IdempotencyRecord;
import br.com.baba.eventHub.core.repository.IdempotencyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class IdempotencyService {

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public <T> Optional<T> findCachedResponse(UUID idempotencyKey, Class<T> clazz) {
        return idempotencyRepository
                .findByIdempotencyKeyAndExpiresAtAfter(idempotencyKey, LocalDateTime.now())
                .map(record -> {
                    try {
                        return objectMapper.readValue(record.getResponseBody(), clazz);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize cached response for key '{}': {}", idempotencyKey, e.getMessage(), e);
                        return null;
                    }
                });
    }

    @Transactional
    public <T> void saveResponse(UUID idempotencyKey, T responseBody) {
        try {
            String json = objectMapper.writeValueAsString(responseBody);
            idempotencyRepository.findByIdempotencyKey(idempotencyKey).ifPresent(record -> {
                record.setResponseBody(json);
                idempotencyRepository.save(record);
            });
        } catch (Exception e) {
            log.error("Failed to save idempotency response for key '{}': {}", idempotencyKey, e.getMessage(), e);
        }
    }

    @Transactional
    public boolean reserveIdempotencyKey(UUID idempotencyKey) {
        try {
            IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, "\"PROCESSING\"");
            idempotencyRepository.saveAndFlush(record);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Transactional
    public void releaseKey(UUID idempotencyKey) {
        idempotencyRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(idempotencyRepository::delete);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredRecords() {
        idempotencyRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
