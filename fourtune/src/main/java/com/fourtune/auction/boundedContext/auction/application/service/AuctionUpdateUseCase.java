package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.shared.auction.dto.AuctionItemUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 경매 수정 UseCase
 * - 경매 정보 수정
 * - 진행중인 경매는 일부만 수정 가능
 */
@Service
@RequiredArgsConstructor
public class AuctionUpdateUseCase {

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;

    /**
     * 경매 정보 수정
     */
    @Transactional
    public void updateAuction(Long auctionId, Long userId, AuctionItemUpdateRequest request) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 판매자 권한 확인
        auctionSupport.validateSeller(auctionItem, userId);
        
        // 3. 수정 가능 여부 확인
        validateUpdateable(auctionItem);
        
        // 4. 엔티티 수정 (엔티티의 update 메서드 호출)
        auctionItem.update(
                request.title(),
                request.description(),
                request.buyNowPrice(),
                request.buyNowPrice() != null  // buyNowEnabled
        );
        
        // 5. DB 저장 (dirty checking으로 자동 저장)
    }

    /**
     * 수정 가능 여부 검증
     */
    private void validateUpdateable(AuctionItem auctionItem) {
        com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus status = 
                auctionItem.getStatus();
        
        // ENDED, SOLD, SOLD_BY_BUY_NOW, CANCELLED 상태는 수정 불가
        if (status == com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.ENDED ||
            status == com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.SOLD ||
            status == com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.SOLD_BY_BUY_NOW ||
            status == com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.CANCELLED) {
            throw new com.fourtune.auction.global.error.exception.BusinessException(
                    com.fourtune.auction.global.error.ErrorCode.AUCTION_NOT_MODIFIABLE
            );
        }
        
        // ACTIVE 상태에서 입찰이 있으면 제한적 수정만 가능
        // (현재는 엔티티의 update 메서드에서 제목, 설명, 즉시구매가만 수정 가능)
    }

}
