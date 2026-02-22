package com.fourtune.payment.port.out;

import com.fourtune.shared.payment.dto.OrderDto;

public interface AuctionPort {
    /**
     * 경매(주문) 모듈로부터 주문 정보를 조회함
     * @param orderId 주문 식별자 (UUID)
     * @return 주문 정보 DTO (가격, 유저정보 등)
     */
    OrderDto getOrder(String orderId);
}
