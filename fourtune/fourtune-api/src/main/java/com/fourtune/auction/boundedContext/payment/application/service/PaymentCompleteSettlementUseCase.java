package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.constant.CashEventType;
import com.fourtune.shared.payment.constant.CashPolicy;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.settlement.dto.SettlementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCompleteSettlementUseCase {
        private final PaymentSupport paymentSupport;

        @Transactional // [중요] 입금/출금의 원자성 보장을 위해 트랜잭션 추가
        public Wallet settlementCashComplete(SettlementDto dto){
                // 동시 정산 시 Lost Update 방지: 시스템·수급자 지갑 비관적 락
                Wallet systemWallet = paymentSupport.findSystemWalletForUpdate().orElseThrow(
                        () -> new BusinessException(ErrorCode.PAYMENT_SYSTEM_WALLET_NOT_FOUND)
                );

                if (systemWallet.getBalance() < dto.getAmount()) {
                        throw new BusinessException(ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE);
                }

                if(dto.getPayeeEmail().equals(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)){
                        Wallet platformWallet = paymentSupport.findPlatformWalletForUpdate().orElseThrow(
                                () -> new BusinessException(ErrorCode.PAYMENT_PLATFORM_WALLET_NOT_FOUND)
                        );


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
