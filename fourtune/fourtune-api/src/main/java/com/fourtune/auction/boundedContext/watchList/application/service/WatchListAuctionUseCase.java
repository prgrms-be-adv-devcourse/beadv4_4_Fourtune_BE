package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.watchList.domain.WatchList;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.watchList.event.WatchListAuctionEndedEvent;
import com.fourtune.shared.watchList.event.WatchListAuctionStartedEvent;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListEventType;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WatchListAuctionUseCase {

    private final WatchListSupport watchListSupport;
    private final EventPublisher eventPublisher;
    private final EventPublishingConfig eventPublishingConfig;
    private final ObjectMapper objectMapper;
    private final WatchListKafkaProducer watchListKafkaProducer;

    public void findAllByAuctionStartItemId(Long auctionItemId){
        List<Long> watchListUsers = watchListSupport.findAllByAuctionItemId(auctionItemId);

        List<Long> targetUsers = watchListUsers.stream()
                .filter(userId -> {
                    WatchList watchList = watchListSupport.findWatchListByUserIdAndAuctionItemId(auctionItemId, userId);
                    return !watchList.isStartAlertSent();
                })
                .toList();

        if (targetUsers.isEmpty()) return;

        publishWatchListEvent(targetUsers, auctionItemId, WatchListEventType.WATCHLIST_AUCTION_STARTED);
        markEndAlertSent(targetUsers, auctionItemId);
    }

    public void findAllByAuctionEndItemId(Long auctionItemId){
        List<Long> watchListUsers = watchListSupport.findAllByAuctionItemId(auctionItemId);
        if(watchListUsers.isEmpty()) return;

        publishWatchListEvent(watchListUsers, auctionItemId, WatchListEventType.WATCHLIST_AUCTION_ENDED);
        markStartAlertSent(watchListUsers, auctionItemId);
    }

    private void publishWatchListEvent(List<Long> users, Long auctionItemId, WatchListEventType type) {
        if (eventPublishingConfig.isWatchlistEventsKafkaEnabled()) {
            try {
                String payload = objectMapper.writeValueAsString(Map.of("users", users, "auctionItemId", auctionItemId));
                watchListKafkaProducer.send(String.valueOf(auctionItemId), payload, type.name());
            } catch (Exception e) {
                log.error("WatchList Kafka 이벤트 발행 실패: auctionItemId={}", auctionItemId, e);
            }
        } else {
            if (type == WatchListEventType.WATCHLIST_AUCTION_STARTED) {
                eventPublisher.publish(new WatchListAuctionStartedEvent(users, auctionItemId));
            } else {
                eventPublisher.publish(new WatchListAuctionEndedEvent(users, auctionItemId));
            }
        }
    }

    private void markStartAlertSent(List<Long> watchListUsers, Long auctionItemId){
        for (Long userId : watchListUsers) {
            WatchList watchList = watchListSupport.findWatchListByUserIdAndAuctionItemId(userId, auctionItemId);
            watchList.markStartAlertSent();
        }
    }

    private void markEndAlertSent(List<Long> watchListUsers, Long auctionItemId){
        for (Long userId : watchListUsers) {
            WatchList watchList = watchListSupport.findWatchListByUserIdAndAuctionItemId(userId, auctionItemId);
            watchList.markEndAlertSent();
        }
    }

}
