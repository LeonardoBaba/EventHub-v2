package br.com.baba.eventHub.payments.service;

import br.com.baba.eventHub.payments.core.dto.PaymentProcessedDTO;
import br.com.baba.eventHub.payments.core.dto.TicketPurchaseDTO;
import br.com.baba.eventHub.payments.core.enums.PaymentStatusEnum;
import br.com.baba.eventHub.payments.core.model.PaymentTransaction;
import br.com.baba.eventHub.payments.core.repository.PaymentTransactionRepository;
import br.com.baba.eventHub.payments.core.util.CardTokenMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Optional;
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
        log.info("Receiving payment request paymentID={} ticketID={}",
                dto.paymentID(), dto.ticketID());

        Optional<PaymentTransaction> existing = repository.findByPaymentId(dto.paymentID());
        PaymentTransaction transaction;

        if (existing.isPresent()) {
            transaction = existing.get();
            log.info("Replay detected paymentID={} - reusing transaction id={} status={}",
                    dto.paymentID(), transaction.getId(), transaction.getStatus());
        } else {
            try {
                simulateProcessingDelay();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Payment processing interrupted paymentID={}", dto.paymentID());
                return;
            }

            PaymentStatusEnum status = ThreadLocalRandom.current().nextBoolean() ? PaymentStatusEnum.SUCCESS : PaymentStatusEnum.FAILED;
            transaction = new PaymentTransaction(
                    dto.ticketID(), dto.paymentID(), CardTokenMasker.mask(dto.cardToken()),
                    dto.installments(), dto.price(), status
            );

            try {
                repository.save(transaction);
                log.info("Transaction saved id={} paymentID={} status={}",
                        transaction.getId(), dto.paymentID(), status);
            } catch (DuplicateKeyException e) {
                transaction = repository.findByPaymentId(dto.paymentID()).orElseThrow();
                log.info("Concurrent duplicate paymentID={} - using existing transaction id={} status={}",
                        dto.paymentID(), transaction.getId(), transaction.getStatus());
            }
        }

        PaymentProcessedDTO response = new PaymentProcessedDTO(
                dto.ticketID(), dto.paymentID(), transaction.getStatus(), transaction.getProcessedAt()
        );

        rabbitTemplate.convertAndSend(exchangeName, outputRoutingKey, response);
        log.info("Response sent routingKey={} paymentID={} status={}",
                outputRoutingKey, dto.paymentID(), transaction.getStatus());
    }

    protected void simulateProcessingDelay() throws InterruptedException {
        int minute = 60000;
        Thread.sleep(ThreadLocalRandom.current().nextInt(5 * minute));
    }
}
