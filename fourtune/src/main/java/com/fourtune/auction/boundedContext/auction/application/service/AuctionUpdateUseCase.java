package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.shared.auction.dto.AuctionItemUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionUpdateUseCase {
    
    private final AuctionSupport auctionSupport;
    
    public AuctionItem update(Long auctionId, AuctionItemUpdateRequest request) {
        // TODO: 경매 수정 로직 구현
        AuctionItem auction = auctionSupport.findByIdOrThrow(auctionId);
        // auction.update(...);
        return auctionSupport.save(auction);
    }
}
