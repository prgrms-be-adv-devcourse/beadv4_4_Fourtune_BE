package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.core.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 경매 도메인 로직 단위 테스트.
 * - 생성 검증(시작가, 기간)
 * - 상태 전이(start, close, update, 입찰/즉시구매/취소)
 */
class AuctionItemTest {

    private static final LocalDateTime START = LocalDateTime.now().plusHours(1);
    private static final LocalDateTime END = LocalDateTime.now().plusDays(1);

    @Test
    @DisplayName("create — 정상 파라미터면 SCHEDULED, currentPrice=startPrice로 생성된다")
    void create_valid_returnsScheduled() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", "설명", "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                START, END
        );

        assertThat(item.getStatus()).isEqualTo(AuctionStatus.SCHEDULED);
        assertThat(item.getCurrentPrice()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
        assertThat(item.getStartPrice()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
        assertThat(item.getSellerId()).isEqualTo(1L);
        assertThat(item.getTitle()).isEqualTo("제목");
        assertThat(item.getCategory()).isEqualTo(Category.ETC);
    }

    @Test
    @DisplayName("create — 시작가 1000원 미만이면 예외")
    void create_startPriceBelowMin_throws() {
        assertThatThrownBy(() -> AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(999), 1000, null, false,
                START, END
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("create — 종료 시각이 시작 이전이면 예외")
    void create_endBeforeStart_throws() {
        assertThatThrownBy(() -> AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                END, START
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("start — SCHEDULED일 때 ACTIVE로 변경된다")
    void start_scheduled_becomesActive() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        assertThat(item.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
    }

    @Test
    @DisplayName("close — ACTIVE일 때 ENDED로 변경된다")
    void close_active_becomesEnded() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        item.close();
        assertThat(item.getStatus()).isEqualTo(AuctionStatus.ENDED);
    }

    @Test
    @DisplayName("update — ACTIVE 상태에서 제목·설명 수정 가능")
    void update_active_updatesFields() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", "기존설명", "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        item.update("새제목", "새설명", null, null);
        assertThat(item.getTitle()).isEqualTo("새제목");
        assertThat(item.getDescription()).isEqualTo("새설명");
    }

    @Test
    @DisplayName("update — ENDED 상태에서는 수정 불가")
    void update_ended_throws() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        item.close();
        assertThatThrownBy(() -> item.update("새제목", null, null, null))
                .isInstanceOf(BusinessException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ELECTRONICS", "CLOTHING", "ETC"})
    @DisplayName("create — 카테고리 문자열로 생성된다")
    void create_category_parsed(String category) {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, category,
                BigDecimal.valueOf(5000), 1000, null, false,
                START, END
        );
        assertThat(item.getCategory().name()).isEqualTo(category);
    }

    // --- 입찰·현재가 로직 ---

    @Test
    @DisplayName("updateCurrentPrice — 유효 금액이면 현재가 갱신된다")
    void updateCurrentPrice_valid_updatesPrice() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        item.updateCurrentPrice(BigDecimal.valueOf(15_000));
        assertThat(item.getCurrentPrice()).isEqualByComparingTo(BigDecimal.valueOf(15_000));
    }

    @Test
    @DisplayName("updateCurrentPrice — null 또는 0 이하면 예외")
    void updateCurrentPrice_invalid_throws() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        assertThatThrownBy(() -> item.updateCurrentPrice(null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> item.updateCurrentPrice(BigDecimal.ZERO)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("increaseBidCount / increaseViewCount — 호출 시 카운트 증가")
    void increaseCounts_increments() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        assertThat(item.getBidCount()).isEqualTo(0);
        assertThat(item.getViewCount()).isEqualTo(0L);
        item.increaseBidCount();
        item.increaseViewCount();
        assertThat(item.getBidCount()).isEqualTo(1);
        assertThat(item.getViewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("canBid — ACTIVE일 때만 true")
    void canBid_onlyActive() {
        AuctionItem scheduled = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        assertThat(scheduled.canBid()).isFalse();
        scheduled.start();
        assertThat(scheduled.canBid()).isTrue();
        scheduled.close();
        assertThat(scheduled.canBid()).isFalse();
    }

    @Test
    @DisplayName("executeBuyNow — ACTIVE + 즉시구매 활성 시 SOLD_BY_BUY_NOW로 변경된다")
    void executeBuyNow_activeAndEnabled_becomesSoldByBuyNow() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, BigDecimal.valueOf(50_000), true,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        item.executeBuyNow();
        assertThat(item.getStatus()).isEqualTo(AuctionStatus.SOLD_BY_BUY_NOW);
        assertThat(item.getCurrentPrice()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
    }

    @Test
    @DisplayName("executeBuyNow — 즉시구매 비활성이면 예외")
    void executeBuyNow_notEnabled_throws() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        assertThatThrownBy(item::executeBuyNow).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("cancel — SCHEDULED이고 입찰 없으면 CANCELLED로 변경된다")
    void cancel_scheduledNoBids_becomesCancelled() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.cancel();
        assertThat(item.getStatus()).isEqualTo(AuctionStatus.CANCELLED);
    }

    // ==================== recoverFromBuyNowFailure 테스트 ====================

    @Test
    @DisplayName("recoverFromBuyNowFailure — SOLD_BY_BUY_NOW → ACTIVE로 전환된다")
    void recoverFromBuyNowFailure_statusBecomesActive() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, BigDecimal.valueOf(50_000), true,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();
        item.executeBuyNow();
        assertThat(item.getStatus()).isEqualTo(AuctionStatus.SOLD_BY_BUY_NOW);

        item.recoverFromBuyNowFailure(10, 3, BigDecimal.valueOf(10_000));

        assertThat(item.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
    }

    @Test
    @DisplayName("recoverFromBuyNowFailure — 종료 시각이 이미 지났으면 KST 기준으로 N분 연장된다")
    void recoverFromBuyNowFailure_expiredEndTime_extendsInKst() {
        LocalDateTime pastEnd = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(5);
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, BigDecimal.valueOf(50_000), true,
                LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(2), pastEnd
        );
        item.start();
        item.executeBuyNow();

        LocalDateTime beforeRecover = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        item.recoverFromBuyNowFailure(10, 3, BigDecimal.valueOf(10_000));

        assertThat(item.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        assertThat(item.getAuctionEndTime()).isAfter(beforeRecover);
        assertThat(item.getAuctionEndTime()).isAfterOrEqualTo(
                beforeRecover.plusMinutes(9)
        );
        assertThat(item.getBuyNowRecoveryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("recoverFromBuyNowFailure — 종료 시각이 아직 남았으면 연장하지 않는다")
    void recoverFromBuyNowFailure_futureEndTime_noExtension() {
        LocalDateTime futureEnd = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(1);
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, BigDecimal.valueOf(50_000), true,
                LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(1), futureEnd
        );
        item.start();
        item.executeBuyNow();
        int countBefore = item.getBuyNowRecoveryCount();

        item.recoverFromBuyNowFailure(10, 3, BigDecimal.valueOf(10_000));

        assertThat(item.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        assertThat(item.getAuctionEndTime()).isEqualTo(futureEnd);
        assertThat(item.getBuyNowRecoveryCount()).isEqualTo(countBefore);
    }

    @Test
    @DisplayName("recoverFromBuyNowFailure — 복구 횟수가 maxRecoveryCount(1) 도달 시 즉시구매 영구 비활성화")
    void recoverFromBuyNowFailure_reachesMaxCount_disablesBuyNow() {
        // auctionEndTime을 과거로 설정하여 연장 조건이 충족되도록 한다
        LocalDateTime pastEnd = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(5);
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, BigDecimal.valueOf(50_000), true,
                LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(2), pastEnd
        );
        item.start();

        // maxRecoveryCount=1 → 1회 연장으로 즉시 Circuit Breaker 발동
        item.executeBuyNow();
        item.recoverFromBuyNowFailure(10, 1, BigDecimal.valueOf(10_000));

        assertThat(item.getBuyNowRecoveryCount()).isEqualTo(1);
        assertThat(item.getBuyNowDisabledByPolicy()).isTrue();
    }

    @Test
    @DisplayName("recoverFromBuyNowFailure — SOLD_BY_BUY_NOW 상태가 아니면 예외")
    void recoverFromBuyNowFailure_notSoldByBuyNow_throws() {
        AuctionItem item = AuctionItem.create(
                1L, "제목", null, "ETC",
                BigDecimal.valueOf(10_000), 1000, null, false,
                LocalDateTime.now().minusHours(1), END
        );
        item.start();

        assertThatThrownBy(() -> item.recoverFromBuyNowFailure(10, 3, BigDecimal.valueOf(10_000)))
                .isInstanceOf(BusinessException.class);
    }
}
