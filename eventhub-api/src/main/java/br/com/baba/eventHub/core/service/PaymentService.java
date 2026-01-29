package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.dto.PaymentProcessedDTO;
import br.com.baba.eventHub.core.dto.TicketFormDTO;
import br.com.baba.eventHub.core.dto.TicketPurchaseDTO;
import br.com.baba.eventHub.core.enums.PaymentStatusEnum;
import br.com.baba.eventHub.core.enums.RabbitQueueEnum;
import br.com.baba.eventHub.core.enums.RoutingKeyEnum;
import br.com.baba.eventHub.core.interfaces.IMessage;
import br.com.baba.eventHub.core.interfaces.IMessageReceive;
import br.com.baba.eventHub.core.model.Payment;
import br.com.baba.eventHub.core.model.Ticket;
import br.com.baba.eventHub.core.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO(payment);
        messageService.send(ticketPurchaseDTO, RoutingKeyEnum.PAYMENT_CREATED);
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
        Optional<Payment> payment = repository.findById(paymentProcessedDTO.paymentID());
        payment.ifPresent(p -> p.setStatus(paymentProcessedDTO.status()));
        ticketService.updateTicketStatus(paymentProcessedDTO.ticketID(), paymentProcessedDTO.status().equals(PaymentStatusEnum.SUCCESS));
    }
}
