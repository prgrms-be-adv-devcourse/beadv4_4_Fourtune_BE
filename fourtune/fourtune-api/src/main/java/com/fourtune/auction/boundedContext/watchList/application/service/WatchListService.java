package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListEventType;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListKafkaProducer;
import com.fourtune.auction.boundedContext.watchList.domain.WatchListAuctionItem;
import com.fourtune.auction.boundedContext.watchList.mapper.WatchListMapper;
import com.fourtune.shared.user.dto.UserResponse;
import com.fourtune.shared.watchList.dto.WatchListResponseDto;
import com.fourtune.shared.watchList.event.WatchListToggleEvent;
import com.fourtune.auction.boundedContext.watchList.application.service.performance.WatchListRedisSetUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchListService {

    private final WatchListSupport watchListSupport;
    private final WatchListSyncUserUseCase watchListSyncUserUseCase;
    private final WatchListSyncAuctionItemUseCase watchListSyncAuctionItemUseCase;
    private final WatchListRedisSetUseCase watchListRedisSetService;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<WatchListKafkaProducer> watchListKafkaProducerProvider;

    @Transactional
    public boolean toggleWatchList(Long userId, Long auctionItemId) {
        boolean isAdded;
        if (isExistWatchList(userId, auctionItemId)) {
            watchListRedisSetService.removeInterest(userId, auctionItemId);
            isAdded = false;
        } else {
            watchListRedisSetService.addInterest(userId, auctionItemId);
            isAdded = true;
        }
        publishToggleEvent(userId, auctionItemId, isAdded);
        return isAdded;
    }

    public List<WatchListResponseDto> getMyWatchLists(Long userId) {
        return watchListSupport.findAllByUserIdWithFetchJoin(userId).stream()
                .map(WatchListMapper::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void syncUser(UserResponse userResponse){
        watchListSyncUserUseCase.syncUser(userResponse);
    }

    @Transactional
    public void syncAuctionItem(Long auctionItemId, String title, BigDecimal currentPrice, String thumbnailUrl, String category){
        watchListSyncAuctionItemUseCase.syncAuctionItem(auctionItemId, title, currentPrice, thumbnailUrl, category);
    }

    public void processAuctionStart(Long auctionItemId){
        watchListRedisSetService.processAuctionStart(auctionItemId);
    }

    public void processAuctionEnd(Long auctionItemId){
        watchListRedisSetService.processAuctionEnd(auctionItemId);
    }

    private boolean isExistWatchList(Long userId, Long itemId) {
        return watchListSupport.existsByUserIdAndAuctionItemId(userId, itemId);
    }

    private void publishToggleEvent(Long userId, Long auctionItemId, boolean isAdded) {
        watchListKafkaProducerProvider.ifAvailable(producer -> {
            try {
                WatchListAuctionItem item = watchListSupport.findOptionalByAuctionItemId(auctionItemId)
                        .orElse(null);
                if (item == null) {
                    log.warn("[WatchList] 토글 이벤트 발행 스킵: 경매 아이템 없음 auctionItemId={}", auctionItemId);
                    return;
                }

                WatchListToggleEvent event = new WatchListToggleEvent(
                        userId,
                        isAdded ? "ADD" : "REMOVE",
                        new WatchListToggleEvent.ItemData(
                                auctionItemId,
                                item.getCategory(),
                                item.getCurrentPrice().longValue(),
                                item.getTitle()
                        )
                );

                String payload = objectMapper.writeValueAsString(event);
                String eventType = isAdded
                        ? WatchListEventType.WATCHLIST_ITEM_ADDED.name()
                        : WatchListEventType.WATCHLIST_ITEM_REMOVED.name();

                producer.send(String.valueOf(userId), payload, eventType);
            } catch (Exception e) {
                log.error("[WatchList] 토글 이벤트 발행 실패: userId={}, auctionItemId={}", userId, auctionItemId, e);
            }
        });
    }

}
