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

        // 혼합 결제: pgAmount는 주문금액의 부족분만 결제하므로 주문금액 이하여야 한다.
        // pgAmount > orderDto.getPrice()인 경우만 오류(초과 결제 방지)
        if (pgAmount > orderDto.getPrice()) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        paymentCashCompleteUseCase.cashComplete(orderDto, pgAmount, paymentKey);
    }
}
