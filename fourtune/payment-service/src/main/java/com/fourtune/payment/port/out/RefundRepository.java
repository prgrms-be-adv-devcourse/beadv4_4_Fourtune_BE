package com.fourtune.payment.port.out;

import com.fourtune.payment.domain.entity.Refund;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    @EntityGraph(attributePaths = {"payment"})
    List<Refund> findRefundsByPayment_PaymentUser_Id(Long paymentPaymentUserId);
}
