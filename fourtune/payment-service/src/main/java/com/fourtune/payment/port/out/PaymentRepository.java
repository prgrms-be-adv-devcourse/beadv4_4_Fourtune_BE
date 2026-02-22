package com.fourtune.payment.port.out;

import com.fourtune.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findPaymentsByPaymentUserId(Long userId);

    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findPaymentByOrderId(String orderId);
}
