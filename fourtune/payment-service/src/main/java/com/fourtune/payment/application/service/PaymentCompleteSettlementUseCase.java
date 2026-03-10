package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.domain.constant.CashEventType;
import com.fourtune.payment.domain.entity.Wallet;
import com.fourtune.payment.port.out.CashLogRepository;
import com.fourtune.shared.payment.constant.CashPolicy;
import com.fourtune.shared.settlement.dto.SettlementDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCompleteSettlementUseCase {
        private final PaymentSupport paymentSupport;
        private final CashLogRepository cashLogRepository;

        @Transactional // [중요] 입금/출금의 원자성 보장을 위해 트랜잭션 추가
        public Wallet settlementCashComplete(SettlementDto dto){
                // 데드락(Deadlock) 방지를 위해 락 획득 순서를 다른 UseCase(결제, 충전 취소 등)와 일치시킵니다.
                // 일관된 락 순서: User Wallet (Payee/Platform) -> System Wallet

                // [DEFECT-002 수정] Kafka at-least-once 중복 이벤트 멱등성 보호.
                // 동일 settlementId에 대한 정산 지급 CashLog가 이미 존재하면 중복 처리로 판단하고 스킵한다.
                CashEventType targetEventType = dto.getPayeeEmail().equals(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                        ? CashEventType.정산지급__상품판매_수수료
                        : CashEventType.정산지급__상품판매_대금;

                if (cashLogRepository.existsByRelTypeCodeAndRelIdAndEventType("settlement", dto.getId(), targetEventType)) {
                        log.warn("중복 정산 지급 이벤트 감지 (Kafka 재전송 추정), 스킵: settlementId={}, payeeEmail={}",
                                dto.getId(), dto.getPayeeEmail());
                        return null;
                }

                if(dto.getPayeeEmail().equals(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)){
                        Wallet platformWallet = paymentSupport.findPlatformWalletForUpdate().orElseThrow(
                                () -> new BusinessException(ErrorCode.PAYMENT_PLATFORM_WALLET_NOT_FOUND)
                        );
                        Wallet systemWallet = paymentSupport.findSystemWalletForUpdate().orElseThrow(
                                () -> new BusinessException(ErrorCode.PAYMENT_SYSTEM_WALLET_NOT_FOUND)
                        );

                        if (systemWallet.getBalance() < dto.getAmount()) {
                                throw new BusinessException(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
                        }

                        systemWallet.debit(
                                dto.getAmount(),
                                CashEventType.정산지급__상품판매_수수료,
                                "settlement",
                                dto.getId()
                        );

                        platformWallet.credit(
                                dto.getAmount(),
                                CashEventType.정산수령__상품판매_수수료,
                                "settlement",
                                dto.getId()
                        );

                        return platformWallet;
                }
                else{
                        Wallet payeeWallet = paymentSupport.findWalletByUserEmailForUpdate(dto.getPayeeEmail()).orElseThrow(
                                () -> new BusinessException(ErrorCode.PAYMENT_WALLET_NOT_FOUND)
                        );
                        Wallet systemWallet = paymentSupport.findSystemWalletForUpdate().orElseThrow(
                                () -> new BusinessException(ErrorCode.PAYMENT_SYSTEM_WALLET_NOT_FOUND)
                        );

                        if (systemWallet.getBalance() < dto.getAmount()) {
                                throw new BusinessException(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
                        }

                        systemWallet.debit(
                                dto.getAmount(),
                                CashEventType.정산지급__상품판매_대금,
                                "settlement",
                                dto.getId()
                        );

                        payeeWallet.credit(
                                dto.getAmount(),
                                CashEventType.정산수령__상품판매_대금,
                                "settlement",
                                dto.getId()
                        );

                        return payeeWallet;
                }
        }
}
