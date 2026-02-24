package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.adapter.in.web.dto.ActiveAuctionsResponse;
import com.fourtune.auction.boundedContext.auction.application.service.AuctionSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내부 전용 API. 다른 서비스(fourtune-api 등)에서 Feign으로 호출.
 * - 탈퇴 처리 시: userId 기준 진행 중(ACTIVE) 경매 존재 여부 확인.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserAuctionController {

    private final AuctionSupport auctionSupport;

    /**
     * 해당 유저(판매자)의 진행 중(ACTIVE) 경매가 있는지 조회.
     * 탈퇴 처리 전 호출하여 진행 중 경매가 있으면 탈퇴 불가/경고 처리에 사용.
     */
    @GetMapping("/{userId}/active-auctions")
    public ResponseEntity<ActiveAuctionsResponse> getActiveAuctionsByUser(@PathVariable Long userId) {
        long count = auctionSupport.countActiveBySellerId(userId);
        return ResponseEntity.ok(ActiveAuctionsResponse.of(count));
    }
}
