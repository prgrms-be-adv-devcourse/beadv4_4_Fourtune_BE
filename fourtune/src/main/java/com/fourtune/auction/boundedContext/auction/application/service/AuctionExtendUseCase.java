package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionExtendUseCase {
    
    private final AuctionSupport auctionSupport;
    
    public void extend(Long auctionId) {
        // TODO: 경매 자동 연장 로직 구현
        // 1. 경매 조회
        // 2. 종료 시간 3분 연장
        // 3. 이벤트 발행
        AuctionItem auction = auctionSupport.findByIdOrThrow(auctionId);
        // auction.extend(Duration.ofMinutes(3));
        auctionSupport.save(auction);
    }
}
