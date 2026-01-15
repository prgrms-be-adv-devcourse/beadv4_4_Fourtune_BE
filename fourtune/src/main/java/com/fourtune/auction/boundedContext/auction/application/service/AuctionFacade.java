package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.shared.auction.dto.AuctionItemCreateRequest;
import com.fourtune.auction.shared.auction.dto.AuctionItemDetailResponse;
import com.fourtune.auction.shared.auction.dto.AuctionItemResponse;
import com.fourtune.auction.shared.auction.dto.AuctionItemUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionFacade {
    
    private final AuctionCreateUseCase auctionCreateUseCase;
    private final AuctionUpdateUseCase auctionUpdateUseCase;
    private final AuctionDeleteUseCase auctionDeleteUseCase;
    private final AuctionQueryUseCase auctionQueryUseCase;
    private final AuctionCloseUseCase auctionCloseUseCase;
    private final AuctionExtendUseCase auctionExtendUseCase;
    private final AuctionSupport auctionSupport;
    
    public AuctionItemResponse createAuction(
        AuctionItemCreateRequest request, 
        List<MultipartFile> images
    ) {
        // TODO: 이미지 업로드 (S3)
        // List<String> imageUrls = s3Service.uploadImages(images);
        
        // 경매 생성
        // AuctionItem auction = auctionCreateUseCase.create(request, imageUrls);
        
        // TODO: Elasticsearch 색인 (비동기)
        
        return null; // AuctionItemResponse.from(auction);
    }
    
    public AuctionItemResponse updateAuction(
        Long auctionId, 
        Long userId, 
        AuctionItemUpdateRequest request
    ) {
        // 권한 확인
        auctionSupport.validateSeller(auctionId, userId);
        
        // 수정
        // AuctionItem auction = auctionUpdateUseCase.update(auctionId, request);
        
        return null; // AuctionItemResponse.from(auction);
    }
    
    public void deleteAuction(Long auctionId, Long userId) {
        // 권한 확인
        auctionSupport.validateSeller(auctionId, userId);
        
        // 삭제
        auctionDeleteUseCase.delete(auctionId);
    }
    
    public AuctionItemDetailResponse getAuctionDetail(Long id) {
        return auctionQueryUseCase.getAuctionDetail(id);
    }
    
    public Page<AuctionItemResponse> getAuctionList(
        AuctionStatus status,
        Category category,
        Pageable pageable
    ) {
        // TODO: 구현
        return null;
    }
    
    public void closeAuction(Long auctionId) {
        auctionCloseUseCase.close(auctionId);
    }
    
    public void extendAuction(Long auctionId) {
        auctionExtendUseCase.extend(auctionId);
    }
    
    public void increaseViewCount(Long auctionId) {
        // TODO: 조회수 증가 로직 (Redis 활용)
    }
}
