package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.domain.constant.CashEventType;
import com.fourtune.payment.domain.constant.PaymentStatus;
import com.fourtune.payment.domain.entity.Payment;
import com.fourtune.payment.domain.entity.PaymentUser;
import com.fourtune.payment.domain.entity.Wallet;
import com.fourtune.payment.port.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지갑 충전 전용 컴포넌트.
 * WalletChargeUseCase에서 PG 호출 전/후 별도 트랜잭션으로 Payment 상태와 지갑 잔액을 관리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletCreditHelper {

    private final PaymentSupport paymentSupport;
    private final PaymentRepository paymentRepository;

    /**
     * PG 호출 전 CHARGE_PENDING 레코드를 저장한다.
     * 서버 크래시 발생 시 이 레코드가 남아 미처리 건 정산 대사(Reconciliation)의 근거가 된다.
     */
    @Transactional
    public Payment savePendingCharge(Long userId, String paymentKey, String orderId, Long amount) {
        PaymentUser paymentUser = paymentSupport.findUserByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_USER_NOT_FOUND));

        Payment pending = Payment.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .auctionOrderId(null)
                .paymentUser(paymentUser)
                .amount(amount)
                .pgPaymentAmount(amount)
                .status(PaymentStatus.CHARGE_PENDING)
                .build();

        Payment saved = paymentRepository.save(pending);
        log.info("지갑 충전 대기 레코드 생성: userId={}, paymentKey={}, amount={}", userId, paymentKey, amount);
        return saved;
    }

    /**
     * PG 성공 후 지갑 잔액을 충전하고 Payment 상태를 APPROVED로 전환한다.
     */
    @Transactional
    public Wallet creditForCharge(Long userId, Long amount, Long paymentId) {
        Wallet wallet = paymentSupport.findWalletByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_WALLET_NOT_FOUND));

        wallet.credit(amount, CashEventType.충전__지갑충전_토스페이먼츠, "Wallet", null);

        paymentRepository.findById(paymentId).ifPresent(Payment::approve);

        log.info("지갑 충전 완료: userId={}, amount={}, newBalance={}", userId, amount, wallet.getBalance());
        return wallet;
    }

    /**
     * PG 실패 또는 보상 트랜잭션 시 Payment 상태를 CANCELED로 전환한다.
     */
    @Transactional
    public void failCharge(Long paymentId) {
        paymentRepository.findById(paymentId).ifPresent(p -> p.cancel("충전 실패 또는 PG 취소"));
        log.info("지갑 충전 취소 처리: paymentId={}", paymentId);
    }
}
