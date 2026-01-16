package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuctionSupport {
    
    private final AuctionItemRepository auctionItemRepository;
    
    public AuctionItem findByIdOrThrow(Long id) {
        return auctionItemRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
    }
    
    public AuctionItem save(AuctionItem auction) {
        return auctionItemRepository.save(auction);
    }
    
    public void validateAuctionModifiable(AuctionItem auction) {
        // TODO: 수정 가능 여부 검증 로직
        // if (auction.getStatus() != AuctionStatus.ACTIVE) {
        //     throw new BusinessException(ErrorCode.AUCTION_NOT_MODIFIABLE);
        // }
    }
    
    public void validateSeller(Long auctionId, Long userId) {
        AuctionItem auction = findByIdOrThrow(auctionId);
        // TODO: 판매자 권한 확인
        // if (!auction.getSellerId().equals(userId)) {
        //     throw new BusinessException(ErrorCode.AUCTION_SELLER_MISMATCH);
        // }
    }
    
    public List<AuctionItem> findExpiredAuctions(LocalDateTime now) {
        return auctionItemRepository.findByAuctionEndTimeBeforeAndStatus(
            now, 
            com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.ACTIVE
        );
    }
}
