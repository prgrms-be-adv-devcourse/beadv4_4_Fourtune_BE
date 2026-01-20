package com.fourtune.auction.boundedContext.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 유형 Enum
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    // 입찰 관련
    BID_PLACED("입찰 알림", "새로운 입찰이 등록되었습니다."),
    BID_OUTBID("상위 입찰 알림", "더 높은 입찰가가 등록되었습니다."),
    
    // 경매 관련
    AUCTION_WON("낙찰 알림", "축하합니다! 경매에 낙찰되었습니다."),
    AUCTION_ENDED("경매 종료 알림", "등록하신 경매가 종료되었습니다."),
    AUCTION_SOON_END("경매 마감 임박", "관심 경매가 곧 종료됩니다."),
    
    // 결제/주문 관련
    PAYMENT_COMPLETED("결제 완료 알림", "결제가 완료되었습니다."),
    ORDER_CREATED("주문 생성 알림", "주문이 생성되었습니다."),
    
    // 관심상품 관련
    WATCHLIST_PRICE_DROP("관심상품 가격 변동", "관심상품의 가격이 변경되었습니다."),
    WATCHLIST_ENDING("관심상품 마감 임박", "관심상품 경매가 곧 종료됩니다."),
    
    // 환불 관련
    REFUND_REQUESTED("환불 요청 접수", "환불 요청이 접수되었습니다."),
    REFUND_APPROVED("환불 승인", "환불이 승인되었습니다."),
    REFUND_REJECTED("환불 거절", "환불이 거절되었습니다."),
    REFUND_COMPLETED("환불 완료", "환불이 완료되었습니다."),
    
    // 정산 관련
    SETTLEMENT_COMPLETED("정산 완료", "정산이 완료되었습니다.");

    private final String description;
    private final String defaultMessage;
}
