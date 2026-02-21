package com.fourtune.recommendation.common;

/**
 * 추천 서비스 공통 상수
 */
public final class RecommendationConstants {

    private RecommendationConstants() {
    }

    // ── 추천 기본 설정 ──
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;
    public static final int TOP_CATEGORY_LIMIT = 3;

    // ── 프로파일링 가중치 ──
    public static final int SEARCH_WEIGHT = 1;
    public static final int WATCHLIST_ADD_WEIGHT = 3;
    public static final int WATCHLIST_REMOVE_WEIGHT = -3;
    public static final int BID_WEIGHT = 5;

    // ── Redis 키 프리픽스 ──
    public static final String USER_METRICS_KEY_PREFIX = "metrics:user:";
    public static final String CATEGORY_FIELD_PREFIX = "category:";
    public static final String RECOMMENDATION_CACHE_PREFIX = "rec:user:";
}
