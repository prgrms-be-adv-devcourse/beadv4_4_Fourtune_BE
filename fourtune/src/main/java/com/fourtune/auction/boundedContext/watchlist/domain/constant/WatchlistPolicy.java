package com.fourtune.auction.boundedContext.watchlist.domain.constant;

/**
 * 관심상품 정책
 */
public class WatchlistPolicy {
    
    // 사용자당 최대 관심상품 등록 개수
    public static final int MAX_WATCHLIST_COUNT = 50;
    
    // 경매 종료 임박 알림 기준 (분)
    public static final int ENDING_SOON_MINUTES = 30;
    
    private WatchlistPolicy() {
        // Utility class
    }
}
