package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.*;
import com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.auction.shared.settlement.dto.SettlementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaymentFacade {

    private final PaymentSupport paymentSupport;
    private final PaymentConfirmUseCase paymentConfirmUseCase;
    private final CompleteSettlementUseCase completeSettlementUseCase;


    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletByUser(PaymentUser paymentUser) {
        return paymentSupport.findWalletByUser(paymentUser);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletByUserId(Long userId) {
        return paymentSupport.findWalletByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletByUserEmail(String email) {
        return paymentSupport.findWalletByUserEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findSystemWallet() {
        return paymentSupport.findSystemWallet();
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findPlatformWallet(){
        return paymentSupport.findPlatformWallet();
    }

    public PaymentExecutionResult confirmPayment(String paymentKey, Long orderId, Long amount) {
        return paymentConfirmUseCase.confirmPayment(paymentKey, orderId, amount);
    }

    public Long getBalance(Long userId) {
        return paymentSupport.getWalletBalanceByUserId(userId);
    }

    public List<CashLog> getCashLogList(Long userId) {
        return paymentSupport.getCashLogList(userId);
    }

    public List<Payment> findPaymentListByUserId(Long userId){
        return paymentSupport.findPaymentListByUserId(userId);
    }

    public List<Refund> findRefundListByUserId(Long userId){
        return paymentSupport.findRefundListByUserId(userId);
    }

    @Transactional
    public Wallet completeSettlement(SettlementDto dto){
        return completeSettlementUseCase.settlementCashComplete(dto);
    }
}
