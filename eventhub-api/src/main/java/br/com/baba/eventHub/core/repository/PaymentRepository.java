package br.com.baba.eventHub.core.repository;

import br.com.baba.eventHub.core.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
