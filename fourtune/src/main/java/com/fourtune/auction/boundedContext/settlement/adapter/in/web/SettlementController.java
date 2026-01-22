package com.fourtune.auction.boundedContext.settlement.adapter.in.web;

import com.fourtune.auction.boundedContext.settlement.adapter.in.web.dto.SettlementCandidatedItemDto;
import com.fourtune.auction.boundedContext.settlement.application.service.SettlementFacade;
import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementFacade settlementFacade;

    /**
     * 최근 정산 내역 조회
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<ApiResponse> getSettlementHistory(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                settlementFacade.findLatestSettlementByUserId(userId)
        ));
    }

    /**
     * 최근순 전체 정산 내역 조회
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<ApiResponse> getAllSettlements(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                settlementFacade.findAllSettlementsByPayeeId(userId)
        ));
    }

    /**
     * 정산 대기 항목 리스트 조회
     */
    @GetMapping("/{userId}/pendings")
    public ResponseEntity<ApiResponse> getSettlementPendings(@PathVariable("userId") Long userId) {
        List<SettlementCandidatedItemDto> dtos = settlementFacade.findSettlementCandidatedItems(userId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}