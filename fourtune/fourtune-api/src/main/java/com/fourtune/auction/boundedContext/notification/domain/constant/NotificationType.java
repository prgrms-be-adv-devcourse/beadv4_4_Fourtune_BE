package com.fourtune.auction.boundedContext.notification.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum NotificationType {
    OUTBID("상위 입찰자가 발생했습니다!", "'%s' 상품의 가격이 %s원으로 갱신되었습니다. 다시 입찰에 도전하세요!"),
    AUCTION_SUCCESS("낙찰 성공!", "'%s' 상품을 %s원에 낙찰받았습니다. 축하합니다!"),
    AUCTION_FAILED("아쉬운 패찰", "'%s' 상품 낙찰에 실패했습니다."),
    PAYMENT("결제 알림", "결제 만료일이 1일 남았습니다"),
    PAYMENT_SUCCESS("결제 성공", "'%s' 상품의 결제가 완료되었습니다."),
    PAYMENT_FAILED("결제 실패", "'%s' 상품의 결제에 실패했습니다."),
    WATCHLIST_START("관심상품 시작 5분 전", "'%s' 상품 경매 시작 5분 전입니다."),
    WATCHLIST_END("관심상품 종료 5분 전", "'%s' 상품 경매 종료 5분 전입니다."),
    SETTLEMENT_SUCCESS("정산 성공", "'%s' 상품의 정산을 성공했습니다."),
    BID_RECEIVED("새로운 입찰 생성", "새로운 입찰이 있습니다."),
    BID_CANCELED("입찰 취소", "입찰을 취소했습니다.");

    private final String titleTemplate;
    private final String contentTemplate;

    NotificationType(String titleTemplate, String contentTemplate) {
        this.titleTemplate = titleTemplate;
        this.contentTemplate = contentTemplate;
    }

    public String getTitle() {
        return titleTemplate;
    }

    public String makeContent(Object... args) {
        return String.format(contentTemplate, args);
    }
}
