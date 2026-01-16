package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.shared.auction.dto.AuctionItemCreateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionCreateUseCase {
    
    private final AuctionSupport auctionSupport;
    
    public AuctionItem create(AuctionItemCreateRequest request, List<String> imageUrls) {
        // TODO: 경매 생성 로직 구현
        // 1. AuctionItem 생성
        // 2. 이미지 생성
        // 3. 저장
        // 4. 이벤트 발행
        
        return null;
    }
}
