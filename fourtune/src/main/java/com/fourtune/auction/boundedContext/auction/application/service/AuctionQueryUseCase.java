package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import com.fourtune.auction.shared.auction.dto.AuctionItemDetailResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionQueryUseCase {
    
    private final AuctionItemRepository auctionItemRepository;
    private final AuctionSupport auctionSupport;
    
    public AuctionItemDetailResponse getAuctionDetail(Long id) {
        // TODO: 경매 상세 조회 로직 구현
        AuctionItem auction = auctionSupport.findByIdOrThrow(id);
        // 조회수 증가
        return null; // AuctionItemDetailResponse.from(auction);
    }
    
    public Page<AuctionItem> getAuctionList(
        AuctionStatus status,
        Category category,
        Pageable pageable
    ) {
        // TODO: 경매 목록 조회 로직 구현
        if (status != null && category != null) {
            return auctionItemRepository.findByStatusAndCategory(status, category, pageable);
        }
        // 다른 조건 처리
        return null;
    }
}
