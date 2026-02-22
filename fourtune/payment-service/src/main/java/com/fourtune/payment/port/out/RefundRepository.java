package com.fourtune.payment.port.out;

import com.fourtune.payment.domain.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findRefundsByPayment_PaymentUser_Id(Long paymentPaymentUserId);
}
