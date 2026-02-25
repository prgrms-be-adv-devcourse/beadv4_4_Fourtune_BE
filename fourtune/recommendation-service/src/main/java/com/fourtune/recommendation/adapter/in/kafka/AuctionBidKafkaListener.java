package com.fourtune.recommendation.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.kafka.KafkaTopicConfig;
import com.fourtune.recommendation.application.service.UserPreferenceService;
import com.fourtune.recommendation.common.RecommendationConstants;
import com.fourtune.shared.auction.event.BidPlacedEvent;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 입찰 이벤트 Consumer (골격)
 * auction-events 토픽에서 BID_PLACED 이벤트를 필터링하여
 * 입찰한 카테고리를 프로파일에 반영 (가중치 +5)
 *
 * TODO: BidPlacedEvent에 category 필드가 추가된 후 활성화
 * (현재는 category 정보가 없어 프로파일링 불가함)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true")
public class AuctionBidKafkaListener {

    private final ObjectMapper objectMapper;
    private final UserPreferenceService userPreferenceService;

    @KafkaListener(topics = KafkaTopicConfig.AUCTION_EVENTS_TOPIC, groupId = "recommendation-auction-group")
    public void consume(String payload, @Header(value = "X-Event-Type", required = false) String eventType) {
        if (eventType == null) {
            return;
        }

        try {
            if (!AuctionEventType.BID_PLACED.name().equals(eventType)) {
                // 추천에서는 BID_PLACED만 관심 대상
                return;
            }

            BidPlacedEvent event = objectMapper.readValue(payload, BidPlacedEvent.class);

            // BidPlacedEvent에 category 필드 추가됨 -> 프로파일링 활성화
            String category = event.category();
            if (category != null && !category.isBlank()) {
                userPreferenceService.incrementCategoryScore(event.bidderId(), category,
                        RecommendationConstants.BID_WEIGHT);
            }

            log.info("[REC][BID] 입찰 이벤트 수신: auctionId={}, bidderId={}, category={}",
                    event.auctionId(), event.bidderId(), category);

        } catch (Exception e) {
            log.error("[REC][BID] 이벤트 처리 실패: eventType={}, error={}", eventType, e.getMessage(), e);
        }
    }
}
