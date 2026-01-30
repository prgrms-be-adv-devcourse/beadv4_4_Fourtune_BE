package com.fourtune.auction.boundedContext.search.adapter.in.event;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.boundedContext.search.application.service.AuctionItemIndexingHandler;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.auction.shared.auction.event.AuctionItemCreatedEvent;
import com.fourtune.auction.shared.auction.event.AuctionItemDeletedEvent;
import com.fourtune.auction.shared.auction.event.AuctionItemUpdatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// AuctionItemIndexEventListener 단위 테스트
@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionItem 이벤트 리스너 테스트")
class AuctionItemIndexEventListenerTest {

    @Mock
    private AuctionItemIndexingHandler indexingHandler;

    @InjectMocks
    private AuctionItemIndexEventListener listener;

    @Test
    @DisplayName("경매 생성 이벤트 수신 시 인덱싱 핸들러 호출")
    void handleCreated_ShouldCallIndexingHandler() {
        // given
        AuctionItemCreatedEvent event = createTestCreatedEvent();

        // when
        listener.handleCreated(event);

        // then
        ArgumentCaptor<SearchAuctionItemView> captor = ArgumentCaptor.forClass(SearchAuctionItemView.class);
        verify(indexingHandler, times(1)).upsert(captor.capture());

        SearchAuctionItemView capturedView = captor.getValue();
        assertThat(capturedView.auctionItemId()).isEqualTo(1L);
        assertThat(capturedView.title()).isEqualTo("Test Auction");
        assertThat(capturedView.category()).isEqualTo("ELECTRONICS");
        assertThat(capturedView.status()).isEqualTo("SCHEDULED");
    }

    @Test
    @DisplayName("경매 수정 이벤트 수신 시 인덱싱 핸들러 호출")
    void handleUpdated_ShouldCallIndexingHandler() {
        // given
        AuctionItemUpdatedEvent event = createTestUpdatedEvent();

        // when
        listener.handleUpdated(event);

        // then
        ArgumentCaptor<SearchAuctionItemView> captor = ArgumentCaptor.forClass(SearchAuctionItemView.class);
        verify(indexingHandler, times(1)).upsert(captor.capture());

        SearchAuctionItemView capturedView = captor.getValue();
        assertThat(capturedView.auctionItemId()).isEqualTo(1L);
        assertThat(capturedView.title()).isEqualTo("Updated Auction");
    }

    @Test
    @DisplayName("경매 삭제 이벤트 수신 시 인덱싱 핸들러 호출")
    void handleDeleted_ShouldCallIndexingHandler() {
        // given
        AuctionItemDeletedEvent event = new AuctionItemDeletedEvent(1L);

        // when
        listener.handleDeleted(event);

        // then
        verify(indexingHandler, times(1)).delete(1L);
    }

    @Test
    @DisplayName("인덱싱 실패 시 예외를 로그만 남기고 전파하지 않음")
    void handleCreated_WhenIndexingFails_ShouldLogErrorWithoutPropagating() {
        // given
        AuctionItemCreatedEvent event = createTestCreatedEvent();
        doThrow(new RuntimeException("ElasticSearch connection failed"))
                .when(indexingHandler).upsert(any());

        // when & then - 예외가 전파되지 않아야 함
        listener.handleCreated(event);

        verify(indexingHandler, times(1)).upsert(any());
    }

    @Test
    @DisplayName("이벤트의 모든 필드가 뷰로 정확히 변환됨")
    void toView_ShouldConvertAllFieldsCorrectly() {
        // given
        LocalDateTime now = LocalDateTime.now();
        AuctionItemCreatedEvent event = new AuctionItemCreatedEvent(
                100L,
                "Complete Test Auction",
                "Detailed description",
                Category.ELECTRONICS,
                AuctionStatus.ACTIVE,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(50000),  // buyNowPrice
                true,                        // buyNowEnabled
                now.minusDays(1),
                now.plusDays(7),
                "https://example.com/thumbnail.jpg",
                now.minusDays(2),
                now,
                500L,
                10,
                25);

        // when
        listener.handleCreated(event);

        // then
        ArgumentCaptor<SearchAuctionItemView> captor = ArgumentCaptor.forClass(SearchAuctionItemView.class);
        verify(indexingHandler).upsert(captor.capture());

        SearchAuctionItemView view = captor.getValue();
        assertThat(view.auctionItemId()).isEqualTo(100L);
        assertThat(view.title()).isEqualTo("Complete Test Auction");
        assertThat(view.description()).isEqualTo("Detailed description");
        assertThat(view.category()).isEqualTo("ELECTRONICS");
        assertThat(view.status()).isEqualTo("ACTIVE");
        assertThat(view.startPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(view.currentPrice()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(view.buyNowPrice()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(view.buyNowEnabled()).isTrue();
        assertThat(view.thumbnailUrl()).isEqualTo("https://example.com/thumbnail.jpg");
        assertThat(view.viewCount()).isEqualTo(500L);
        assertThat(view.bidCount()).isEqualTo(10);
        assertThat(view.watchlistCount()).isEqualTo(25);
    }

    // Helper methods
    private AuctionItemCreatedEvent createTestCreatedEvent() {
        return new AuctionItemCreatedEvent(
                1L,
                "Test Auction",
                "Test Description",
                Category.ELECTRONICS,
                AuctionStatus.SCHEDULED,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(50000),  // buyNowPrice
                true,                        // buyNowEnabled
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7),
                "https://example.com/image.jpg",
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L,
                0,
                0);
    }

    private AuctionItemUpdatedEvent createTestUpdatedEvent() {
        return new AuctionItemUpdatedEvent(
                1L,
                "Updated Auction",
                "Updated Description",
                Category.ELECTRONICS,
                AuctionStatus.ACTIVE,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(60000),  // buyNowPrice
                true,                        // buyNowEnabled
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(6),
                "https://example.com/image.jpg",
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now(),
                100L,
                5,
                10);
    }
}
