package com.fourtune.payment.port.out;


import com.fourtune.payment.domain.vo.PaymentExecutionResult;

public interface PaymentGatewayPort {
    /**
     * 결제 승인 요청 (Toss API 호출)
     * @return 결제 성공 정보 (paymentKey 등)
     */
    PaymentExecutionResult confirm(String paymentKey, String orderId, Long amount);

    /**
     * 결제 취소 요청 (보상 트랜잭션용)
     * 내부 로직 실패 시 이미 승인된 결제를 취소하기 위함
     */
    PaymentExecutionResult cancel(String paymentKey, String reason, Long requestAmount);
}
