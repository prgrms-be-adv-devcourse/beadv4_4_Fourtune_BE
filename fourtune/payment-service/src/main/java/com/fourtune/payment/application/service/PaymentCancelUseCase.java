package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.domain.constant.PaymentStatus;
import com.fourtune.payment.domain.entity.Payment;
import com.fourtune.payment.domain.entity.Refund;
import com.fourtune.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.payment.port.out.PaymentGatewayPort;
import com.fourtune.payment.port.out.PaymentRepository;
import com.fourtune.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCancelUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentRefundRetryRecorder paymentRefundRetryRecorder;
    private final PaymentCancelCompletion paymentCancelCompletion;

    /**
     * 결제 취소 (환불) — PG 호출을 먼저 수행하고, 성공 시 DB 처리는 별도 트랜잭션에서 락·저장·이벤트 처리.
     * 락 유지 중 외부(PG) 호출을 하지 않아 지연 시 결제/정산 전반 블로킹을 방지한다.
     */
    public Refund cancelPayment(String cancelReason, Long cancelAmount, OrderDto orderDto) {
        // 1. [조회] 주문에 해당하는 결제 내역 조회
        Payment payment = paymentRepository.findPaymentByOrderId(orderDto.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 2. [검증] 이미 취소된 건인지 확인
        if (payment.getStatus() == PaymentStatus.CANCELED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        // 3. [금액 확정] 부분 취소 금액이 없으면 남은 잔액 전체를 취소
        Long requestAmount = (cancelAmount == null) ? payment.getBalanceAmount() : cancelAmount;

        // 4. [검증] 취소 가능 잔액 확인 (PG 결제 잔액)
        if (payment.getBalanceAmount() < requestAmount) {
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_AMOUNT_EXCEEDS_BALANCE);
        }

        // 5. [PG 취소 선행] 락 없이 PG만 먼저 호출 (지연 시 DB 락 유지 방지)
        PaymentExecutionResult result;
        try {
            result = paymentGatewayPort.cancel(payment.getPaymentKey(), cancelReason, requestAmount);
        } catch (Exception e) {
            paymentRefundRetryRecorder.recordRefundPgRetry(payment.getPaymentKey(), orderDto.getOrderId(), requestAmount, cancelReason);
            throw new BusinessException(ErrorCode.PAYMENT_PG_REFUND_FAILED);
        }

        if (!result.isSuccess()) {
            throw new BusinessException(ErrorCode.PAYMENT_PG_REFUND_FAILED);
        }

        // 6. [DB 처리] 별도 트랜잭션: 락 획득 → 지갑 입출금 → 상태/Refund 저장 → 이벤트 발행
        return paymentCancelCompletion.completeCancelInDb(payment, orderDto, requestAmount, cancelReason);
    }
}
