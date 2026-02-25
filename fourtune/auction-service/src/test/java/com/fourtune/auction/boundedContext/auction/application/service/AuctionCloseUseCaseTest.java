package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.port.out.UserPort;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.outbox.service.OutboxService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * AuctionCloseUseCase 단위 테스트.
 * 핵심 검증:
 * - Feign 실패 시 낙찰/종료 상태 전환이 롤백되지 않음
 * - validateCloseable()이 KST 기준으로 동작하는지 확인
 */
@ExtendWith(MockitoExtension.class)
class AuctionCloseUseCaseTest {

    @Mock AuctionSupport auctionSupport;
    @Mock BidSupport bidSupport;
    @Mock OrderCreateUseCase orderCreateUseCase;
    @Mock EventPublisher eventPublisher;
    @Mock UserPort userPort;
    @Mock EventPublishingConfig eventPublishingConfig;
    @Mock OutboxService outboxService;

    @InjectMocks
    AuctionCloseUseCase sut;

    // ==================== Feign 장애 복원력 — 낙찰자 있는 경우 ====================

    @Test
    @DisplayName("낙찰자 있음 + Feign 정상 — SOLD 저장 및 Outbox 2건 발행")
    void closeAuction_withWinner_feignSuccess_savesSold() {
        AuctionItem auction = buildActiveAuction(1L);
        Bid winningBid = buildBid(1L, 2L, BigDecimal.valueOf(20_000));

        when(auctionSupport.findByIdWithLockOrThrow(1L)).thenReturn(auction);
        when(bidSupport.findHighestBid(1L)).thenReturn(Optional.of(winningBid));
        when(orderCreateUseCase.createWinningOrder(any(AuctionItem.class), any(), any())).thenReturn("ORDER-001");
        when(userPort.getNicknamesByIds(any())).thenReturn(java.util.Map.of(1L, "판매자"));
        when(eventPublishingConfig.isAuctionEventsKafkaEnabled()).thenReturn(true);

        sut.closeAuction(1L);

        verify(auctionSupport).findByIdWithLockOrThrow(1L);
        verify(outboxService, times(2)).append(any(), any(), any(), any());
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.SOLD);
    }

    @Test
    @DisplayName("낙찰자 있음 + Feign 타임아웃 — 예외 전파 없이 낙찰 처리 완료")
    void closeAuction_withWinner_feignTimeout_doesNotThrow() {
        AuctionItem auction = buildActiveAuction(1L);
        Bid winningBid = buildBid(1L, 2L, BigDecimal.valueOf(20_000));

        when(auctionSupport.findByIdWithLockOrThrow(1L)).thenReturn(auction);
        when(bidSupport.findHighestBid(1L)).thenReturn(Optional.of(winningBid));
        when(orderCreateUseCase.createWinningOrder(any(AuctionItem.class), any(), any())).thenReturn("ORDER-001");
        when(userPort.getNicknamesByIds(any())).thenThrow(new RuntimeException("Feign read timeout"));
        when(eventPublishingConfig.isAuctionEventsKafkaEnabled()).thenReturn(true);

        // Feign 실패해도 예외가 전파되지 않아야 한다
        assertThatCode(() -> sut.closeAuction(1L)).doesNotThrowAnyException();

        // 낙찰 상태로 저장되었는지 확인
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.SOLD);
        // Outbox는 AuctionClosedEvent + AuctionItemUpdatedEvent(sellerName=null) 2건
        verify(outboxService, times(2)).append(any(), any(), any(), any());
    }

    // ==================== Feign 장애 복원력 — 낙찰자 없는 경우 ====================

    @Test
    @DisplayName("낙찰자 없음 + Feign 타임아웃 — 예외 전파 없이 ENDED 처리 완료")
    void closeAuction_noWinner_feignTimeout_doesNotThrow() {
        AuctionItem auction = buildActiveAuction(1L);

        when(auctionSupport.findByIdWithLockOrThrow(1L)).thenReturn(auction);
        when(bidSupport.findHighestBid(1L)).thenReturn(Optional.empty());
        when(userPort.getNicknamesByIds(any())).thenThrow(new RuntimeException("fourtune-api down"));
        when(eventPublishingConfig.isAuctionEventsKafkaEnabled()).thenReturn(true);

        assertThatCode(() -> sut.closeAuction(1L)).doesNotThrowAnyException();

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.ENDED);
        verify(outboxService, times(2)).append(any(), any(), any(), any());
    }

    // ==================== validateCloseable KST 검증 ====================

    @Test
    @DisplayName("validateCloseable — 종료 시각이 이미 지난 ACTIVE 경매는 정상 처리된다")
    void closeAuction_pastEndTime_processesSuccessfully() {
        // auctionEndTime을 KST 기준 과거로 설정
        AuctionItem auction = buildActiveAuctionWithEndTime(
                1L, LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(5)
        );

        when(auctionSupport.findByIdWithLockOrThrow(1L)).thenReturn(auction);
        when(bidSupport.findHighestBid(1L)).thenReturn(Optional.empty());
        when(userPort.getNicknamesByIds(any())).thenReturn(java.util.Map.of());
        when(eventPublishingConfig.isAuctionEventsKafkaEnabled()).thenReturn(true);

        assertThatCode(() -> sut.closeAuction(1L)).doesNotThrowAnyException();
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.ENDED);
    }

    // ==================== 헬퍼 ====================

    private AuctionItem buildActiveAuction(Long id) {
        return buildActiveAuctionWithEndTime(
                id, LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(1)
        );
    }

    private AuctionItem buildActiveAuctionWithEndTime(Long id, LocalDateTime endTime) {
        return AuctionItem.builder()
                .id(id)
                .sellerId(1L)
                .title("테스트 경매")
                .description("설명")
                .category(Category.ETC)
                .startPrice(BigDecimal.valueOf(10_000))
                .bidUnit(1000)
                .buyNowEnabled(false)
                .buyNowPrice(null)
                .auctionStartTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(2))
                .auctionEndTime(endTime)
                .status(AuctionStatus.ACTIVE)
                .currentPrice(BigDecimal.valueOf(10_000))
                .viewCount(0L)
                .watchlistCount(0)
                .bidCount(0)
                .extensionCount(0)
                .buyNowRecoveryCount(0)
                .buyNowDisabledByPolicy(false)
                .images(new ArrayList<>())
                .build();
    }

    private Bid buildBid(Long auctionId, Long bidderId, BigDecimal amount) {
        return Bid.create(auctionId, bidderId, amount, BigDecimal.valueOf(10_000), 1000, false);
    }
}
