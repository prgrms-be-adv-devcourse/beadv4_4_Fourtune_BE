package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.domain.entity.*;
import com.fourtune.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.payment.port.out.AuctionPort;
import com.fourtune.payment.port.out.CashLogRepository;
import com.fourtune.shared.payment.dto.OrderDto;
import com.fourtune.shared.payment.dto.PaymentUserDto;
import com.fourtune.shared.settlement.dto.SettlementDto;
import com.fourtune.shared.user.dto.UserResponse;
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
    private final PaymentCompleteSettlementUseCase paymentCompleteSettlementUseCase;
    private final PaymentSyncUserUseCase paymentSyncUserUseCase;
    private final PaymentCreateWalletUseCase paymentCreateWalletUseCase;
    private final PaymentCancelUseCase paymentCancelUseCase;
    private final CashLogRepository cashLogRepository;
    private final AuctionPort auctionPort;


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

    public PaymentExecutionResult confirmPayment(String paymentKey, String orderId, Long amount, Long userId) {
        return paymentConfirmUseCase.confirmPayment(paymentKey, orderId, amount, userId);
    }

    @Transactional(readOnly = true)
    public Long getBalance(Long userId) {
        return paymentSupport.getWalletBalanceByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<CashLog> getCashLogList(Long userId) {
        return paymentSupport.getCashLogList(userId);
    }

    @Transactional(readOnly = true)
    public List<CashLog> getRecentCashLogs(Long userId, int size){
        return paymentSupport.findSliceCashLogs(userId, 0, size);
    }

    @Transactional(readOnly = true)
    public List<CashLog> findSliceCashLogs(Long userId, int page, int size){
        return paymentSupport.findSliceCashLogs(userId, page, size);
    }

    @Transactional(readOnly = true)
    public List<Payment> findPaymentListByUserId(Long userId){
        return paymentSupport.findPaymentListByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Refund> findRefundListByUserId(Long userId){
        return paymentSupport.findRefundListByUserId(userId);
    }

    @Transactional
    public Wallet completeSettlement(SettlementDto dto){
        return paymentCompleteSettlementUseCase.settlementCashComplete(dto);
    }

    @Transactional
    public Wallet createWallet(PaymentUserDto dto){
        return paymentCreateWalletUseCase.createWallet(dto);
    }

    @Transactional
    public PaymentUser syncUser(UserResponse userResponse){
        return paymentSyncUserUseCase.syncUser(userResponse);
    }

    @Transactional
    public void deleteUser(UserResponse user) {
        paymentSupport.deleteUser(user);
    }

    /**
     * 결제 취소 (내부 API용). orderId로 주문 정보를 조회한 뒤 취소 처리.
     */
    public Refund cancelPayment(String orderId, String cancelReason, Long cancelAmount) {
        OrderDto orderDto = auctionPort.getOrder(orderId);
        if (orderDto == null) {
            throw new BusinessException(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND);
        }
        return paymentCancelUseCase.cancelPayment(cancelReason, cancelAmount, orderDto);
    }
}
