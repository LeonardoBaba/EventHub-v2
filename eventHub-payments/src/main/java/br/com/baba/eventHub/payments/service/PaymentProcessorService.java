package br.com.baba.eventHub.payments.service;

import br.com.baba.eventHub.payments.core.dto.PaymentProcessedDTO;
import br.com.baba.eventHub.payments.core.dto.TicketPurchaseDTO;
import br.com.baba.eventHub.payments.core.model.PaymentTransaction;
import br.com.baba.eventHub.payments.core.repository.PaymentTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class PaymentProcessorService {

    @Autowired
    private PaymentTransactionRepository repository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${mq.exchange.name}")
    private String exchangeName;

    @Value("${mq.routing.key.output}")
    private String outputRoutingKey;

    @RabbitListener(queues = "${mq.queue.input}")
    public void processPayment(TicketPurchaseDTO dto) {
        System.out.println("Receiving payment request ID: " + dto.paymentID());
        try {
            simulateProcessingDelay();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Payment processing interrupted paymentID={}", dto.paymentID());
            return;
        }
        try {
            boolean isSuccess = ThreadLocalRandom.current().nextBoolean();
            String status = isSuccess ? "SUCCESS" : "FAILED";

            PaymentTransaction transaction = new PaymentTransaction(
                    dto.ticketID(), dto.paymentID(), dto.cardToken(),
                    dto.installments(), dto.price(), status
            );
            repository.save(transaction);
            log.info("Transaction saved id={} paymentID={} status={}",
                    transaction.getId(), dto.paymentID(), status);

            PaymentProcessedDTO response = new PaymentProcessedDTO(
                    dto.ticketID(), dto.paymentID(), status, LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend(exchangeName, outputRoutingKey, response);
            log.info("Response sent routingKey={} paymentID={} status={}",
                    outputRoutingKey, dto.paymentID(), status);

        } catch (Exception e) {
            log.error("Failed to process payment paymentID={} ticketID={}",
                    dto.paymentID(), dto.ticketID(), e);
        }
    }

    protected void simulateProcessingDelay() throws InterruptedException {
        int minute = 60000;
        Thread.sleep(ThreadLocalRandom.current().nextInt(5 * minute));
    }
}