package br.com.baba.eventHub.payments.service;

import br.com.baba.eventHub.payments.core.dto.PaymentProcessedDTO;
import br.com.baba.eventHub.payments.core.dto.TicketPurchaseDTO;
import br.com.baba.eventHub.payments.core.model.PaymentTransaction;
import br.com.baba.eventHub.payments.core.repository.PaymentTransactionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
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
    public void processPayment(TicketPurchaseDTO ticketPurchaseDTO) {
        System.out.println("Receiving payment request ID: " + ticketPurchaseDTO.paymentID());

        try {
            System.out.println("Processing...");
            int minutes = 60000;
            Thread.sleep(new Random().nextInt(30 * minutes));

            boolean isSuccess = new Random().nextBoolean();
            String status = isSuccess ? "SUCCESS" : "FAILED";

            PaymentTransaction transaction = new PaymentTransaction(
                    ticketPurchaseDTO.ticketID(),
                    ticketPurchaseDTO.paymentID(),
                    ticketPurchaseDTO.cardToken(),
                    ticketPurchaseDTO.installments(),
                    status
            );
            repository.save(transaction);
            System.out.println("Transaction saved: " + transaction.getId());

            PaymentProcessedDTO response = new PaymentProcessedDTO(
                    ticketPurchaseDTO.ticketID(),
                    ticketPurchaseDTO.paymentID(),
                    status,
                    LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend(exchangeName, outputRoutingKey, response);
            System.out.println("Response sent to queue: " + outputRoutingKey + " | Status: " + status);

        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
        }
    }
}