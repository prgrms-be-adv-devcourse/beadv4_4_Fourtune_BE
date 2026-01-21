package com.fourtune.auction.shared.auction.dto;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.OrderType;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 상세 응답 DTO
 */
public record OrderDetailResponse(
    Long id,
    String orderId,
    Long auctionId,
    String auctionTitle,
    String thumbnailUrl,
    Long winnerId,
    String winnerNickname,
    Long sellerId,
    String sellerNickname,
    BigDecimal finalPrice,
    OrderType orderType,
    OrderStatus status,
    String paymentKey,
    LocalDateTime paidAt,
    LocalDateTime createdAt
) {
    /**
     * Order와 AuctionItem으로 OrderDetailResponse 생성
     */
    public static OrderDetailResponse from(Order order, AuctionItem auctionItem) {
        String thumbnailUrl = null;
        if (auctionItem != null && auctionItem.getImages() != null) {
            thumbnailUrl = auctionItem.getImages().stream()
                    .filter(ItemImage::getIsThumbnail)
                    .findFirst()
                    .map(ItemImage::getImageUrl)
                    .orElse(null);
        }
        
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderId(),
                order.getAuctionId(),
                auctionItem != null ? auctionItem.getTitle() : null,
                thumbnailUrl,
                order.getWinnerId(),
                null, // winnerNickname은 User 조회 필요
                order.getSellerId(),
                null, // sellerNickname은 User 조회 필요
                order.getAmount(),
                determineOrderType(auctionItem),
                order.getStatus(),
                null, // paymentKey는 결제 완료 후 설정 (Order 엔티티에 없음)
                order.getPaidAt(),
                order.getCreatedAt()
        );
    }
    
    /**
     * 주문 타입 결정 (경매 낙찰 or 즉시구매)
     */
    private static OrderType determineOrderType(AuctionItem auctionItem) {
        if (auctionItem == null) {
            return OrderType.AUCTION_WIN;
        }
        return switch (auctionItem.getStatus()) {
            case SOLD_BY_BUY_NOW -> OrderType.BUY_NOW;
            default -> OrderType.AUCTION_WIN;
        };
    }
}
