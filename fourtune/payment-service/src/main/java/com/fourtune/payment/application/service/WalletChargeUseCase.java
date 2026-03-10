package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.domain.entity.Payment;
import com.fourtune.payment.domain.entity.Wallet;
import com.fourtune.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.payment.port.out.PaymentGatewayPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 지갑 사전 충전 UseCase.
 * 주문(낙찰/즉시구매)과 무관하게 사용자가 원하는 금액만큼 Toss PG로 충전한다.
 *
 * 처리 순서:
 * 1. 금액 유효성 검증
 * 2. CHARGE_PENDING 레코드 저장 (크래시 발생 시 미처리 건 정산 대사용)
 * 3. Toss PG confirm (DB 락 없이 — 외부 HTTP 호출 중 DB 커넥션 점유 방지)
 * 4. PG 성공 후 지갑 credit + Payment APPROVED 전환 (WalletCreditHelper: 별도 트랜잭션 + 비관적 락)
 * 5. 지갑 credit 실패 시 PG 자동 취소 + Payment CANCELED 전환 (보상 트랜잭션)
 *
 * [크래시 내성]
 * PG confirm 성공 직후 서버 크래시가 발생해도 CHARGE_PENDING 레코드가 남아있어
 * 배치 또는 운영팀이 정산 대사(Reconciliation)를 수행할 수 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletChargeUseCase {

    private static final long MIN_CHARGE_AMOUNT = 1_000L;
    private static final long MAX_CHARGE_AMOUNT = 5_000_000L;

    private final PaymentGatewayPort paymentGatewayPort;
    private final WalletCreditHelper walletCreditHelper;

    public Wallet charge(Long userId, String paymentKey, String orderId, Long amount) {
        if (amount < MIN_CHARGE_AMOUNT) {
            throw new BusinessException(ErrorCode.PAYMENT_WALLET_CHARGE_AMOUNT_TOO_SMALL);
        }
        if (amount > MAX_CHARGE_AMOUNT) {
            throw new BusinessException(ErrorCode.PAYMENT_WALLET_CHARGE_AMOUNT_TOO_LARGE);
        }

        // PG 호출 전에 CHARGE_PENDING 레코드를 먼저 저장한다.
        // 서버 크래시 시에도 이 레코드가 남아 미처리 충전 건을 추적할 수 있다.
        Payment pendingPayment = walletCreditHelper.savePendingCharge(userId, paymentKey, orderId, amount);

        PaymentExecutionResult result = paymentGatewayPort.confirm(paymentKey, orderId, amount);
        if (!result.isSuccess()) {
            walletCreditHelper.failCharge(pendingPayment.getId());
            throw new BusinessException(ErrorCode.PAYMENT_PG_FAILED);
        }

        try {
            return walletCreditHelper.creditForCharge(userId, amount, pendingPayment.getId());
        } catch (Exception e) {
            log.error("지갑 충전 DB 처리 실패. PG 취소를 시도합니다. paymentKey={}, error={}", paymentKey, e.getMessage());
            try {
                paymentGatewayPort.cancel(paymentKey, "지갑 충전 내부 오류로 인한 자동 취소", amount);
                walletCreditHelper.failCharge(pendingPayment.getId());
            } catch (Exception cancelEx) {
                log.error("CRITICAL: 지갑 충전 PG 취소 실패 (수동 환불 필요). paymentKey={}", paymentKey);
            }
            throw e;
        }
    }
}
