package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType;
import com.fourtune.auction.boundedContext.payment.domain.constant.PaymentStatus;
import com.fourtune.auction.boundedContext.payment.domain.entity.Payment;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Refund;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentGatewayPort;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentRepository;
import com.fourtune.auction.boundedContext.payment.port.out.RefundRepository;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCancelUseCase {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentSupport paymentSupport;
    /**
     * 결제 취소 (환불) 요청
     * @param cancelReason 취소 사유
     * @param cancelAmount 취소 요청 금액 (null이면 전액 취소)
     * @param orderDto 취소할 주문 데이터
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

        PaymentUser paymentUser = payment.getPaymentUser();
        Wallet payerWallet = paymentSupport.findWalletByUser(paymentUser).orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_WALLET_NOT_FOUND)
        );

        Wallet systemWallet = paymentSupport.findSystemWallet().orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_SYSTEM_WALLET_NOT_FOUND)
        );

        // 시스템 지갑에서 돈을 뺄 수 있는지 확인하고 차감 (잔액 부족 시 예외 발생 -> 롤백됨)
        if(systemWallet.getBalance() < cancelAmount){
            log.warn("결제 취소 실패 - 시스템 지갑 잔액 부족. user={}, amount={}", payment.getPaymentUser().getId(), requestAmount);
            throw new BusinessException(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
        }
        systemWallet.debit(requestAmount, CashEventType.환불__주문취소__결제금액, "Order", orderDto.getAuctionOrderId());
        payerWallet.credit(requestAmount, CashEventType.환불__주문취소__결제금액, "Order", orderDto.getAuctionOrderId());

        // 6. [외부] PG사 취소 요청
        // 내부 지갑 정리가 끝났으므로 실제 돈을 돌려줌
        PaymentExecutionResult result = paymentGatewayPort.cancel(payment.getPaymentKey(), cancelReason, requestAmount);

        // 7. [상태 변경] DB 업데이트
        if (result.isSuccess()) {
            payment.decreaseBalance(requestAmount); // 내부적으로 balance 차감 및 status 변경(전액/부분환불)
        }

        Refund refund = Refund.create(payment, requestAmount, cancelReason, null);

        refundRepository.save(refund);
        paymentRepository.save(payment);

        return refund;
    }
}
