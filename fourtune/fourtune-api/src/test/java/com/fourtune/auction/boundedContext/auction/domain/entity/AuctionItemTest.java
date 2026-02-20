package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuctionItemTest {

    private static AuctionItem createAuctionItem(AuctionStatus status) {
        return AuctionItem.builder()
                .sellerId(1L)
                .title("테스트 경매")
                .description("설명")
                .category(Category.ELECTRONICS)
                .startPrice(BigDecimal.valueOf(10000))
                .currentPrice(BigDecimal.valueOf(10000))
                .bidUnit(1000)
                .auctionStartTime(LocalDateTime.now().minusHours(1))
                .auctionEndTime(LocalDateTime.now().plusHours(1))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("즉시구매 취소 시 SOLD_BY_BUY_NOW → ACTIVE로 복구된다")
    void releaseFromBuyNow_SOLD_BY_BUY_NOW_ThenBecomesACTIVE() {
        AuctionItem auction = createAuctionItem(AuctionStatus.SOLD_BY_BUY_NOW);

        auction.releaseFromBuyNow();

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
    }

    @Test
    @DisplayName("즉시구매 취소 시 SOLD_BY_BUY_NOW가 아니면 AUCTION_NOT_MODIFIABLE 예외가 발생한다")
    void releaseFromBuyNow_NotSOLD_BY_BUY_NOW_Throws() {
        AuctionItem auction = createAuctionItem(AuctionStatus.ACTIVE);

        assertThatThrownBy(auction::releaseFromBuyNow)
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.AUCTION_NOT_MODIFIABLE));
    }
}
