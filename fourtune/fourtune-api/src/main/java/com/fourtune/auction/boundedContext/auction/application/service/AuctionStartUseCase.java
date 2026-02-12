package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.user.application.service.UserFacade;
import com.fourtune.common.global.eventPublisher.EventPublisher;
import com.fourtune.common.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.common.shared.auction.event.AuctionStartedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuctionStartUseCase {

    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;
    private final UserFacade userFacade;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startAuctionInTransaction(Long auctionId) {
        AuctionItem auction = auctionSupport.findByIdOrThrow(auctionId);
        auction.start();
        auctionSupport.save(auction);

        // 경매 시작 이벤트 발행 (관심상품 알림용)
        eventPublisher.publish(new AuctionStartedEvent(
                auction.getId(),
                auction.getTitle(),
                auction.getSellerId(),
                auction.getStartPrice(),
                auction.getAuctionEndTime()));

        // Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
        String thumbnailUrl = extractThumbnailUrl(auction);
        String sellerName = userFacade.getNicknamesByIds(Set.of(auction.getSellerId())).getOrDefault(auction.getSellerId(), null);

        eventPublisher.publish(new AuctionItemUpdatedEvent(
                auction.getId(),
                auction.getSellerId(),
                sellerName,
                auction.getTitle(),
                auction.getDescription(),
                auction.getCategory().toString(),
                auction.getStatus().toString(),
                auction.getStartPrice(),
                auction.getCurrentPrice(),
                auction.getBuyNowPrice(),
                auction.getBuyNowEnabled(),
                auction.getAuctionStartTime(),
                auction.getAuctionEndTime(),
                thumbnailUrl,
                auction.getCreatedAt(),
                auction.getUpdatedAt(),
                auction.getViewCount(),
                auction.getBidCount(),
                auction.getWatchlistCount()));
    }

    private String extractThumbnailUrl(AuctionItem auctionItem) {
        if (auctionItem.getImages() == null || auctionItem.getImages().isEmpty()) {
            return null;
        }
        return auctionItem.getImages().stream()
                .filter(ItemImage::getIsThumbnail)
                .findFirst()
                .map(ItemImage::getImageUrl)
                .orElseGet(() -> auctionItem.getImages().get(0).getImageUrl()); // 썸네일이 없으면 첫 번째 이미지
    }

}
