package br.com.baba.eventHub.payments.service;

import br.com.baba.eventHub.payments.core.dto.PaymentProcessedDTO;
import br.com.baba.eventHub.payments.core.dto.TicketPurchaseDTO;
import br.com.baba.eventHub.payments.core.model.PaymentTransaction;
import br.com.baba.eventHub.payments.core.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorServiceTest {

    @Mock
    private PaymentTransactionRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    @Spy
    private PaymentProcessorService paymentProcessorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentProcessorService, "exchangeName", "testExchange");
        ReflectionTestUtils.setField(paymentProcessorService, "outputRoutingKey", "testRoutingKey");
    }

    @Test
    void processPayment_ShouldSaveTransactionAndSendMessage_WithoutDelay() throws Exception {
        UUID ticketId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        TicketPurchaseDTO purchaseDTO = new TicketPurchaseDTO(
                ticketId,
                paymentId,
                "token-789",
                1
        );

        doNothing().when(paymentProcessorService).simulateProcessingDelay();

        paymentProcessorService.processPayment(purchaseDTO);

        verify(paymentProcessorService, times(1)).simulateProcessingDelay();

        ArgumentCaptor<PaymentTransaction> transactionCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(repository, times(1)).save(transactionCaptor.capture());

        PaymentTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(ticketId, savedTransaction.getTicketId());
        assertEquals(paymentId, savedTransaction.getPaymentId());
        assertEquals("token-789", savedTransaction.getCardToken());
        assertEquals(1, savedTransaction.getInstallments());
        assertTrue("SUCCESS".equals(savedTransaction.getStatus()) || "FAILED".equals(savedTransaction.getStatus()));

        ArgumentCaptor<PaymentProcessedDTO> rabbitCaptor = ArgumentCaptor.forClass(PaymentProcessedDTO.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq("testExchange"), eq("testRoutingKey"), rabbitCaptor.capture());

        PaymentProcessedDTO messageSent = rabbitCaptor.getValue();
        assertEquals(ticketId, messageSent.ticketID());
        assertEquals(paymentId, messageSent.paymentID());
        assertEquals(savedTransaction.getStatus(), messageSent.status());
        assertNotNull(messageSent.processedDate());
    }

    @Test
    void processPayment_ShouldCatchAndLogError_WhenExceptionIsThrown() throws Exception {
        TicketPurchaseDTO purchaseDTO = new TicketPurchaseDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token-error",
                1
        );

        doThrow(new InterruptedException("Simulated delay error")).when(paymentProcessorService).simulateProcessingDelay();

        paymentProcessorService.processPayment(purchaseDTO);

        verify(paymentProcessorService, times(1)).simulateProcessingDelay();

        verify(repository, times(0)).save(any());
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
