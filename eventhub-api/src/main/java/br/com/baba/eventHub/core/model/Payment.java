package br.com.baba.eventHub.core.model;

import br.com.baba.eventHub.core.dto.TicketFormDTO;
import br.com.baba.eventHub.core.enums.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatusEnum status;

    private String cardToken;

    private Integer installments;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    public Payment(Ticket ticket, TicketFormDTO ticketFormDTO) {
        this.ticket = ticket;
        this.creationDate = LocalDateTime.now();
        this.status = PaymentStatusEnum.PENDING;
        this.cardToken = ticketFormDTO.cardToken();
        this.installments = ticketFormDTO.installments();
    }
}
