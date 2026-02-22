package com.fourtune.auction.boundedContext.settlement.adapter.in.web;

import com.fourtune.auction.boundedContext.settlement.adapter.in.web.dto.SettlementCandidatedItemDto;
import com.fourtune.auction.boundedContext.settlement.adapter.in.web.dto.SettlementResponse;
import com.fourtune.auction.boundedContext.settlement.application.service.SettlementFacade;
import com.fourtune.core.dto.ApiResponse;
import com.fourtune.shared.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementFacade settlementFacade;

    /**
     * 최근 정산 내역 조회
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<SettlementResponse>> getSettlementHistory(@AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(ApiResponse.success(
                settlementFacade.findLatestSettlementByUserId(user.id())
        ));
    }

    /**
     * 최근순 전체 정산 내역 조회
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getAllSettlements(@AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(ApiResponse.success(
                settlementFacade.findAllSettlementsByPayeeId(user.id())
        ));
    }

    /**
     * 정산 대기 항목 리스트 조회
     */
    @GetMapping("/pendings")
    public ResponseEntity<ApiResponse<List<SettlementCandidatedItemDto>>> getSettlementPendings(@AuthenticationPrincipal UserContext user) {
        List<SettlementCandidatedItemDto> dtos = settlementFacade.findSettlementCandidatedItems(user.id());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
