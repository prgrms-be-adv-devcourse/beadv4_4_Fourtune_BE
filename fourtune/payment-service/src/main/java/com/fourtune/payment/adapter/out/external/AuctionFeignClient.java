package com.fourtune.payment.adapter.out.external;

import com.fourtune.core.dto.ApiResponse;
import com.fourtune.shared.auction.dto.OrderDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auction-service", url = "${api.auction.base-url}")
public interface AuctionFeignClient {

    @GetMapping("/api/v1/orders/public/{orderId}")
    ApiResponse<OrderDetailResponse> getOrder(@PathVariable("orderId") String orderId);
}