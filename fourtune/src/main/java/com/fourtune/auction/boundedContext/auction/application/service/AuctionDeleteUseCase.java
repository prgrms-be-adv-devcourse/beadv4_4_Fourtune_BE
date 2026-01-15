package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionDeleteUseCase {
    
    private final AuctionItemRepository auctionItemRepository;
    private final AuctionSupport auctionSupport;
    
    public void delete(Long auctionId) {
        // TODO: 경매 삭제 로직 구현
        AuctionItem auction = auctionSupport.findByIdOrThrow(auctionId);
        // 검증 로직
        auctionItemRepository.delete(auction);
    }
}
