package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionCloseUseCase {
    
    private final AuctionSupport auctionSupport;
    
    public void close(Long auctionId) {
        // TODO: 경매 종료 처리 로직 구현
        // 1. 경매 조회
        // 2. 낙찰자 결정 (최고 입찰자)
        // 3. 상태 변경 (ENDED → SOLD)
        // 4. 이벤트 발행
        AuctionItem auction = auctionSupport.findByIdOrThrow(auctionId);
        // auction.close();
        auctionSupport.save(auction);
    }
}
