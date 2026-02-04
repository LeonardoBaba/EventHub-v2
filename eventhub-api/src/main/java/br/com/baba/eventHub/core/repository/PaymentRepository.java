package br.com.baba.eventHub.core.repository;

import br.com.baba.eventHub.core.enums.PaymentStatusEnum;
import br.com.baba.eventHub.core.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByStatus(PaymentStatusEnum status);

    List<Payment> findByStatusAndCreationDateBefore(PaymentStatusEnum status, LocalDateTime creationDateBefore);
}
