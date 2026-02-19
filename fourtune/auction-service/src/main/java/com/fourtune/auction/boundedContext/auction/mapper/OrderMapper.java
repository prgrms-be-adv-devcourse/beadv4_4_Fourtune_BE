package com.fourtune.auction.boundedContext.auction.mapper;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderType;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import com.fourtune.common.shared.auction.dto.OrderDetailResponse;
import com.fourtune.common.shared.auction.dto.OrderResponse;
import org.springframework.stereotype.Component;

public class OrderMapper {

    /**
     * Order와 AuctionItem으로 OrderResponse 생성
     */
    public static OrderResponse from(Order order, AuctionItem auctionItem) {
        return from(order, auctionItem, null);
    }

    /**
     * Order, AuctionItem, winnerNickname으로 OrderResponse 생성
     */
    public static OrderResponse from(Order order, AuctionItem auctionItem, String winnerNickname) {
        return new OrderResponse(
                order.getId(),
                order.getOrderId(),
                order.getAuctionId(),
                auctionItem != null ? auctionItem.getTitle() : null,
                order.getWinnerId(),
                winnerNickname,
                order.getAmount(),
                determineOrderType(auctionItem),
                order.getStatus().toString(),
                order.getCreatedAt()
        );
    }

    /**
     * Order만으로 OrderResponse 생성 (auctionTitle 없음)
     */
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderId(),
                order.getAuctionId(),
                null,
                order.getWinnerId(),
                null,
                order.getAmount(),
                null,
                order.getStatus().toString(),
                order.getCreatedAt()
        );
    }

    /**
     * 주문 타입 결정 (경매 낙찰 or 즉시구매)
     */
    private static String determineOrderType(AuctionItem auctionItem) {
        if (auctionItem == null) {
            return OrderType.AUCTION_WIN.toString();
        }
        return switch (auctionItem.getStatus()) {
            case SOLD_BY_BUY_NOW -> OrderType.BUY_NOW.toString();
            default -> OrderType.AUCTION_WIN.toString();
        };
    }

    public static OrderDetailResponse fromDetail(Order order, AuctionItem auctionItem) {
        return fromDetail(order, auctionItem, null, null);
    }

    /**
     * Order, AuctionItem, winnerNickname, sellerNickname으로 OrderDetailResponse 생성
     */
    public static OrderDetailResponse fromDetail(Order order, AuctionItem auctionItem,
                                           String winnerNickname, String sellerNickname) {
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
                winnerNickname,
                order.getSellerId(),
                sellerNickname,
                order.getAmount(),
                determineOrderType(auctionItem),
                order.getStatus().toString(),
                null, // paymentKey는 결제 완료 후 설정 (Order 엔티티에 없음)
                order.getPaidAt(),
                order.getCreatedAt()
        );
    }

}
