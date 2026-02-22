package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.port.out.AuctionPort;
import com.fourtune.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentConfirmInternalUseCase {

    private final AuctionPort auctionPort;
    private final PaymentCashCompleteUseCase paymentCashCompleteUseCase;

    @Transactional
    public void processInternalSystemLogic(String orderId, Long pgAmount, String paymentKey, Long userId) {
        OrderDto orderDto = auctionPort.getOrder(orderId);

        if (orderDto == null) {
            throw new BusinessException(ErrorCode.PAYMENT_AUCTION_ORDER_NOT_FOUND);
        }

        if (!orderDto.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PAYMENT_PURCHASE_NOT_ALLOWED);
        }

        if (!"PENDING".equals(orderDto.getOrderStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_ORDER_NOT_PENDING);
        }

        if (!orderDto.getPrice().equals(pgAmount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        paymentCashCompleteUseCase.cashComplete(orderDto, pgAmount, paymentKey);
    }
}
