package com.fourtune.auction.boundedContext.user.adapter.out.external;

import com.fourtune.auction.boundedContext.user.adapter.out.external.dto.ActiveAuctionsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * auction-service 내부 API 호출용 Feign 클라이언트.
 * - 탈퇴 처리 시: userId 기준 진행 중(ACTIVE) 경매 존재 여부 확인.
 */
@FeignClient(name = "auction-service", url = "${api.auction.base-url}")
public interface AuctionServiceClient {

    /**
     * 해당 유저(판매자)의 진행 중(ACTIVE) 경매가 있는지 조회.
     */
    @GetMapping("/internal/users/{userId}/active-auctions")
    ActiveAuctionsResponse getActiveAuctionsByUser(@PathVariable("userId") Long userId);
}
