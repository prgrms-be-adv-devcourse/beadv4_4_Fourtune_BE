package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.event.AuctionExtendedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 경매 자동 연장 UseCase
 * - 종료 5분 전 입찰 시 자동 연장
 * - 연장 횟수 제한
 */
@Service
@RequiredArgsConstructor
public class AuctionExtendUseCase {

    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;

    /**
     * 경매 자동 연장
     */
    @Transactional
    public void extendAuction(Long auctionId) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 연장 가능 여부 확인
        //    - 종료 5분 전인지
        //    - 연장 횟수 제한 확인
        // 3. 경매 시간 연장 (AuctionPolicy.EXTENSION_MINUTES 만큼)
        // 4. 연장 횟수 증가
        // 5. 이벤트 발행 (AuctionExtendedEvent)
    }

    /**
     * 자동 연장 필요 여부 확인
     */
    public boolean needsExtension(Long auctionId, LocalDateTime bidTime) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 종료 시간과 입찰 시간 차이 계산
        // 3. AuctionPolicy.EXTENSION_THRESHOLD_MINUTES 이내면 true
        return false;
    }

    /**
     * 연장 가능 여부 검증
     */
    private void validateExtendable(AuctionItem auctionItem) {
        // TODO: 구현 필요
        // - ACTIVE 상태인지
        // - 연장 횟수가 최대 횟수 미만인지
    }

}
