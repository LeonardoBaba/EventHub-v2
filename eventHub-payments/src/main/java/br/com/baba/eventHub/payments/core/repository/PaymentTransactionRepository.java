package br.com.baba.eventHub.payments.core.repository;

import br.com.baba.eventHub.payments.core.model.PaymentTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends MongoRepository<PaymentTransaction, String> {

    Optional<PaymentTransaction> findByPaymentId(UUID paymentId);
}
