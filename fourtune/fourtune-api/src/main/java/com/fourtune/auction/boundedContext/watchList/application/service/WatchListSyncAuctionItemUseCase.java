package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fourtune.auction.boundedContext.watchList.domain.WatchListAuctionItem;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListItemsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchListSyncAuctionItemUseCase {

    private final WatchListSupport watchListSupport;
    private final WatchListItemsRepository watchListItemsRepository;

    @Transactional
    public void syncAuctionItem(Long auctionItemId, String title, BigDecimal currentPrice, String thumbnailUrl, String category) {
        log.info("관심상품 경매 물품 동기화 시작 - ItemId: {}", auctionItemId);

        watchListSupport.findOptionalByAuctionItemId(auctionItemId)
                .ifPresentOrElse(
                        existingItem -> {
                            existingItem.updateSync(
                                    title,
                                    currentPrice,
                                    thumbnailUrl,
                                    category
                            );
                            log.info("기존 관심상품 Replica 업데이트 완료 : {}", auctionItemId);
                        },
                        () -> {
                            WatchListAuctionItem newItem = WatchListAuctionItem.builder()
                                    .id(auctionItemId)
                                    .title(title)
                                    .currentPrice(currentPrice)
                                    .thumbnailImageUrl(thumbnailUrl)
                                    .category(category)
                                    .build();

                            watchListItemsRepository.save(newItem);
                            log.info("새로운 관심상품 Replica 생성 완료 : {}", auctionItemId);
                        }
                );
    }
}
