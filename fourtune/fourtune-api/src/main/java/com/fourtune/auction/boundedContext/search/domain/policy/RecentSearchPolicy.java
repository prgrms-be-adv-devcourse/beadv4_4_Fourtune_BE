package com.fourtune.auction.boundedContext.search.domain.policy;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RecentSearchPolicy {

    private static final int MAX_KEYWORDS = 10;
    private static final Duration TTL = Duration.ofDays(30);
    private static final String KEY_PREFIX = "recent_search:";

    public int getMaxKeywords() {
        return MAX_KEYWORDS;
    }

    public Duration getTtl() {
        return TTL;
    }

    public String getKeyPrefix() {
        return KEY_PREFIX;
    }
}
