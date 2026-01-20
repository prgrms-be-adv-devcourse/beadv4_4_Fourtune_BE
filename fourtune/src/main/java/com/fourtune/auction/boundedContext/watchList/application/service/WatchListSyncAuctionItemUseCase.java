package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fourtune.auction.boundedContext.watchList.domain.WatchListAuctionItem;
import com.fourtune.auction.boundedContext.watchList.port.out.WatchListItemsRepository;
import com.fourtune.auction.shared.auction.dto.AuctionItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchListSyncAuctionItemUseCase {

    private final WatchListSupport watchListSupport;
    private final WatchListItemsRepository watchListItemsRepository;

    @Transactional
    public void syncAuctionItem(AuctionItemResponse response) {
        log.info("관심상품 경매 물품 동기화 시작 - ItemId: {}", response.id());

        watchListSupport.findOptionalByAuctionItemId(response.id())
                .ifPresentOrElse(
                        existingItem -> {
                            existingItem.updateSync(
                                    response.title(),
                                    response.currentPrice(),
                                    response.thumbnailUrl()
                            );
                            log.info("기존 관심상품 Replica 업데이트 완료 : {}", response.id());
                        },
                        () -> {
                            WatchListAuctionItem newItem = WatchListAuctionItem.builder()
                                    .id(response.id())
                                    .title(response.title())
                                    .currentPrice(response.currentPrice())
                                    .thumbnailImageUrl(response.thumbnailUrl())
                                    .build();

                            watchListItemsRepository.save(newItem);
                            log.info("새로운 관심상품 Replica 생성 완료 : {}", response.id());
                        }
                );
    }
}
