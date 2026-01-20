package com.fourtune.auction.boundedContext.settlement.adapter.in.web;

import com.fourtune.auction.boundedContext.settlement.application.service.SettlementFacade;
import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementFacade settlementFacade;

    @GetMapping("{userId}/history")
    public ApiResponse getSettlementHistory(@PathVariable("userId") Long userId) {
        Settlement settlement = settlementFacade.findLatestSettlementByUserId(userId);
        return ApiResponse.success(settlement);
    }
}
