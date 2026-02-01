package br.com.baba.eventHub.payments.core.repository;

import br.com.baba.eventHub.payments.core.model.PaymentTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentTransactionRepository extends MongoRepository<PaymentTransaction, String> {
}