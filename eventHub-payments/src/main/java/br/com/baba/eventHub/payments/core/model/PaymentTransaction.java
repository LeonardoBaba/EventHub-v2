package br.com.baba.eventHub.payments.core.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Document(collection = "transactions")
public class PaymentTransaction {

    @Id
    private String id;
    private UUID ticketId;
    private UUID paymentId;
    private String cardToken;
    private Integer installments;
    private String status;
    private LocalDateTime processedAt;

    public PaymentTransaction(UUID ticketId, UUID paymentId, String cardToken, Integer installments, String status) {
        this.ticketId = ticketId;
        this.paymentId = paymentId;
        this.cardToken = cardToken;
        this.installments = installments;
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }
}