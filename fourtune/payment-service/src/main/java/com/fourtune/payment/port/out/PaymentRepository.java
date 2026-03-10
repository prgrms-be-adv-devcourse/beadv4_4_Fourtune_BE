package com.fourtune.payment.port.out;

import com.fourtune.payment.domain.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"paymentUser"})
    List<Payment> findPaymentsByPaymentUserId(Long userId);

    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findPaymentByOrderId(String orderId);

    /** 환불 동시성 제어용 — 비관적 락으로 Payment 재조회 (SELECT ... FOR UPDATE) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);
}
