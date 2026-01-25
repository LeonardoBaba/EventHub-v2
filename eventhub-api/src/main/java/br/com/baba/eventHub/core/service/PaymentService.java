package br.com.baba.eventHub.core.service;

import br.com.baba.eventHub.core.dto.TicketFormDTO;
import br.com.baba.eventHub.core.model.Payment;
import br.com.baba.eventHub.core.model.Ticket;
import br.com.baba.eventHub.core.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository repository;

    @Transactional
    public void savePayment(Ticket ticket, TicketFormDTO ticketFormDTO) {
        Payment payment = new Payment(ticket, ticketFormDTO);
        repository.save(payment);
    }
}
