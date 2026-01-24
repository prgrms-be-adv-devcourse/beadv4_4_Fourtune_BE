package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.event.AuctionDeletedEvent;
import com.fourtune.auction.shared.auction.event.AuctionItemDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 경매 삭제 UseCase
 * - 경매 삭제 (soft delete or hard delete)
 * - 입찰이 있으면 삭제 불가
 */
@Service
@RequiredArgsConstructor
public class AuctionDeleteUseCase {

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;
    private final AuctionItemRepository auctionItemRepository;
    private final EventPublisher eventPublisher;

    /**
     * 경매 삭제
     */
    @Transactional
    public void deleteAuction(Long auctionId, Long userId) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 판매자 권한 확인
        auctionSupport.validateSeller(auctionItem, userId);
        
        // 3. 삭제 가능 여부 확인
        auctionSupport.validateDeletable(auctionItem);
        
        // 4. 삭제 전 정보 저장 (이벤트 발행용)
        Long deletedAuctionId = auctionItem.getId();
        Long sellerId = auctionItem.getSellerId();
        String title = auctionItem.getTitle();
        com.fourtune.auction.boundedContext.auction.domain.constant.Category category = 
                auctionItem.getCategory();
        
        // 5. 삭제 처리 (hard delete)
        auctionItemRepository.delete(auctionItem);
        
        // 6. 이벤트 발행
        eventPublisher.publish(new AuctionDeletedEvent(
                deletedAuctionId,
                sellerId,
                title,
                category
        ));
        
        // 7. Search 인덱싱 전용 이벤트 발행 (auctionId만 필요)
        eventPublisher.publish(new AuctionItemDeletedEvent(deletedAuctionId));
    }

    /**
     * 경매 취소 (soft delete)
     */
    @Transactional
    public void cancelAuction(Long auctionId, Long userId) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 판매자 권한 확인
        auctionSupport.validateSeller(auctionItem, userId);
        
        // 3. 취소 가능 여부 확인 (입찰이 있으면 취소 불가)
        if (auctionItem.getBidCount() > 0) {
            throw new com.fourtune.auction.global.error.exception.BusinessException(
                    com.fourtune.auction.global.error.ErrorCode.AUCTION_HAS_BIDS
            );
        }
        
        // 4. 상태 변경 (CANCELLED)
        auctionItem.cancel();
    }

}
