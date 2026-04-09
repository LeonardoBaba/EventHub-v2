package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.dto.EventResponseDTO;
import br.com.baba.eventHub.core.enums.EventStatusEnum;
import br.com.baba.eventHub.core.model.IdempotencyRecord;
import br.com.baba.eventHub.core.repository.IdempotencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ReflectionTestUtils.setField(idempotencyService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("Should return empty when key is not found")
    void shouldReturnEmptyWhenKeyNotFound() {
        UUID key = UUID.randomUUID();
        when(idempotencyRepository.findByIdempotencyKeyAndExpiresAtAfter(any(), any()))
                .thenReturn(Optional.empty());

        Optional<EventResponseDTO> result = idempotencyService.findCachedResponse(key, EventResponseDTO.class);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return cached DTO when key is valid and not expired")
    void shouldReturnDtoWhenKeyIsValid() throws Exception {
        UUID key = UUID.randomUUID();
        EventResponseDTO expectedDTO = new EventResponseDTO(
                UUID.randomUUID(), "Test Event", "Description",
                LocalDateTime.now().plusDays(1), "Location", 100, 5000, EventStatusEnum.ACTIVE
        );

        String json = objectMapper.writeValueAsString(expectedDTO);
        IdempotencyRecord record = new IdempotencyRecord(key, json);

        when(idempotencyRepository.findByIdempotencyKeyAndExpiresAtAfter(eq(key), any()))
                .thenReturn(Optional.of(record));

        Optional<EventResponseDTO> result = idempotencyService.findCachedResponse(key, EventResponseDTO.class);

        assertTrue(result.isPresent());
        assertEquals(expectedDTO.id(), result.get().id());
        assertEquals(expectedDTO.title(), result.get().title());
    }

    @Test
    @DisplayName("Should return empty when record contains invalid JSON")
    void shouldReturnEmptyWhenJsonIsInvalid() {
        UUID key = UUID.randomUUID();
        IdempotencyRecord record = new IdempotencyRecord(key, "not-valid-json");

        when(idempotencyRepository.findByIdempotencyKeyAndExpiresAtAfter(eq(key), any()))
                .thenReturn(Optional.of(record));

        Optional<EventResponseDTO> result = idempotencyService.findCachedResponse(key, EventResponseDTO.class);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should update existing record when saving response")
    void shouldUpdateExistingRecordWhenSavingResponse() {
        UUID key = UUID.randomUUID();
        IdempotencyRecord existingRecord = new IdempotencyRecord(key, "\"PROCESSING\"");

        when(idempotencyRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existingRecord));

        EventResponseDTO dto = new EventResponseDTO(
                UUID.randomUUID(), "Test Event", "Description",
                LocalDateTime.now().plusDays(1), "Location", 100, 5000, EventStatusEnum.ACTIVE
        );

        idempotencyService.saveResponse(key, dto);

        verify(idempotencyRepository).save(argThat(record ->
                key.equals(record.getIdempotencyKey()) && !record.getResponseBody().equals("\"PROCESSING\"")
        ));
    }

    @Test
    @DisplayName("Should not throw when saving fails silently")
    void shouldNotThrowWhenSavingFailsSilently() {
        UUID key = UUID.randomUUID();
        IdempotencyRecord existingRecord = new IdempotencyRecord(key, "\"PROCESSING\"");
        when(idempotencyRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existingRecord));
        when(idempotencyRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        EventResponseDTO dto = new EventResponseDTO(
                UUID.randomUUID(), "Test Event", "Description",
                LocalDateTime.now().plusDays(1), "Location", 100, 5000, EventStatusEnum.ACTIVE
        );

        assertDoesNotThrow(() -> idempotencyService.saveResponse(key, dto));
    }

    @Test
    @DisplayName("Should return false when reserve fails due to integrity violation")
    void shouldReturnFalseWhenReserveFails() {
        UUID key = UUID.randomUUID();
        when(idempotencyRepository.saveAndFlush(any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));
        assertFalse(idempotencyService.reserveIdempotencyKey(key));
    }

    @Test
    @DisplayName("Should return true when reserve succeeds")
    void shouldReturnTrueWhenReserveSucceeds() {
        UUID key = UUID.randomUUID();
        when(idempotencyRepository.saveAndFlush(any())).thenReturn(new IdempotencyRecord());
        assertTrue(idempotencyService.reserveIdempotencyKey(key));
    }
}
