package br.com.baba.eventHub.payments.core.model;

import br.com.baba.eventHub.contracts.PaymentStatusEnum;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Document(collection = "transactions")
public class PaymentTransaction {

    @Id
    private String id;
    private UUID ticketId;

    @Indexed(unique = true)
    private UUID paymentId;
    private String cardToken;
    private Integer installments;
    private Integer price;
    private PaymentStatusEnum status;
    private LocalDateTime processedAt;

    public PaymentTransaction(UUID ticketId, UUID paymentId, String cardToken, Integer installments, Integer price, PaymentStatusEnum status) {
        this.ticketId = ticketId;
        this.paymentId = paymentId;
        this.cardToken = cardToken;
        this.installments = installments;
        this.price = price;
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }
}