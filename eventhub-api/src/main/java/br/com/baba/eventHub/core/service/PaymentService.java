package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.contracts.PaymentProcessedDTO;
import br.com.baba.eventHub.contracts.PaymentStatusEnum;
import br.com.baba.eventHub.contracts.TicketPurchaseDTO;
import br.com.baba.eventHub.core.dto.TicketFormDTO;
import br.com.baba.eventHub.core.enums.RabbitQueueEnum;
import br.com.baba.eventHub.core.enums.RoutingKeyEnum;
import br.com.baba.eventHub.core.interfaces.IMessage;
import br.com.baba.eventHub.core.interfaces.IMessageReceive;
import br.com.baba.eventHub.core.model.Payment;
import br.com.baba.eventHub.core.model.Ticket;
import br.com.baba.eventHub.core.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentService implements IMessageReceive<PaymentProcessedDTO> {

    @Autowired
    private PaymentRepository repository;

    @Autowired
    private IMessage messageService;

    @Autowired
    @Lazy
    private TicketService ticketService;

    @Transactional
    public void processPayment(Ticket ticket, TicketFormDTO ticketFormDTO) {
        Payment payment = new Payment(ticket, ticketFormDTO);
        repository.save(payment);
        TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO(
                payment.getTicket().getId(), payment.getId(), payment.getCardToken(),
                payment.getInstallments(), payment.getPrice());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messageService.send(ticketPurchaseDTO, RoutingKeyEnum.PAYMENT_CREATED);
            }
        });
    }

    @Override
    public String getQueue() {
        return RabbitQueueEnum.QUEUE_PAYMENT_PROCESSED.getName();
    }

    @Override
    public String getRoutingKey() {
        return RoutingKeyEnum.PAYMENT_PROCESSED.getRoutingName();
    }

    @Override
    public Class<PaymentProcessedDTO> getPayloadType() {
        return PaymentProcessedDTO.class;
    }

    @Override
    @Transactional
    public void processMessage(PaymentProcessedDTO paymentProcessedDTO) {
        Optional<Payment> paymentOpt = repository.findById(paymentProcessedDTO.paymentID());
        if (paymentOpt.isEmpty()) {
            log.warn("Received payment.processed for unknown payment id={}", paymentProcessedDTO.paymentID());
            return;
        }
        Payment payment = paymentOpt.get();
        if (payment.getStatus() != PaymentStatusEnum.PENDING) {
            log.info("Payment {} already processed (status={}), skipping redelivery", payment.getId(), payment.getStatus());
            return;
        }
        payment.setStatus(paymentProcessedDTO.status());
        payment.setProcessedDate(paymentProcessedDTO.processedDate());
        ticketService.updateTicketStatus(paymentProcessedDTO.ticketID(), paymentProcessedDTO.status() == PaymentStatusEnum.SUCCESS);
    }

    @Scheduled(fixedRate = 60000 * 5)
    @Transactional
    public void checkTicketsPending() {
        List<Payment> payments = repository.findByStatusAndCreationDateBefore(PaymentStatusEnum.PENDING, LocalDateTime.now().minusHours(1));
        payments.forEach(p -> {
            p.setProcessedDate(LocalDateTime.now());
            p.setStatus(PaymentStatusEnum.TIMEOUT);
            ticketService.updateTicketStatus(p.getTicket().getId(), false);
        });
    }
}
