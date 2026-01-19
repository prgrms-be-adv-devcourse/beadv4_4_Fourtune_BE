package com.fourtune.auction.boundedContext.notification.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum NotificationType {
    OUTBID("상위 입찰자가 발생했습니다!", "'%s' 상품의 가격이 %s원으로 갱신되었습니다. 다시 입찰에 도전하세요!"),
    AUCTION_SUCCESS("낙찰 성공!", "'%s' 상품을 %s원에 낙찰받았습니다. 축하합니다!"),
    AUCTION_FAILED("아쉬운 패찰", "'%s' 상품 낙찰에 실패했습니다."),
    PAYMENT("결제 알림", "결제 만료일이 1일 남았습니다"),
    WATCHLIST_START("관심상품 시작 5분 전", "'%s' 상품 경매 시작 5분 전입니다."),
    WATCHLIST_END("관심상품 종료 5분 전", "'%s' 상품 경매 종료 5분 전입니다.");

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
