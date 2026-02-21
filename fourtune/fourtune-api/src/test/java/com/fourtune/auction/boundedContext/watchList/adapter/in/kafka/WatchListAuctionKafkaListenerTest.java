package com.fourtune.auction.boundedContext.watchList.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.watchList.application.service.WatchListService;
import com.fourtune.shared.auction.event.*;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchListAuctionKafkaListenerTest {

    @Mock
    private WatchListService watchListService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WatchListAuctionKafkaListener listener;

    // ── AUCTION_ITEM_CREATED
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("AUCTION_ITEM_CREATED: syncAuctionItem(id, title, price, thumbnail) 호출")
    void consume_AuctionItemCreated() throws Exception {
        // Given
        String payload = "{}";
        AuctionItemCreatedEvent event = new AuctionItemCreatedEvent(
                1L, 10L, "판매자", "경매상품", "설명", "ELECTRONICS",
                "SCHEDULED", BigDecimal.valueOf(1000), BigDecimal.valueOf(1000),
                BigDecimal.valueOf(5000), true,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                "https://thumbnail.url", LocalDateTime.now(), LocalDateTime.now(),
                0L, 0, 0);

        when(objectMapper.readValue(payload, AuctionItemCreatedEvent.class)).thenReturn(event);

        // When
        listener.consume(payload, AuctionEventType.AUCTION_ITEM_CREATED.name());

        // Then
        verify(watchListService).syncAuctionItem(1L, "경매상품", BigDecimal.valueOf(1000), "https://thumbnail.url");
    }

    // ── AUCTION_ITEM_UPDATED
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("AUCTION_ITEM_UPDATED: syncAuctionItem(id, title, price, thumbnail) 호출")
    void consume_AuctionItemUpdated() throws Exception {
        // Given
        String payload = "{}";
        AuctionItemUpdatedEvent event = mock(AuctionItemUpdatedEvent.class);

        when(objectMapper.readValue(payload, AuctionItemUpdatedEvent.class)).thenReturn(event);
        when(event.auctionItemId()).thenReturn(2L);
        when(event.title()).thenReturn("수정된상품");
        when(event.currentPrice()).thenReturn(BigDecimal.valueOf(2000));
        when(event.thumbnailUrl()).thenReturn("https://updated.url");

        // When
        listener.consume(payload, AuctionEventType.AUCTION_ITEM_UPDATED.name());

        // Then
        verify(watchListService).syncAuctionItem(2L, "수정된상품", BigDecimal.valueOf(2000), "https://updated.url");
    }

    // ── AUCTION_STARTING_SOON
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("AUCTION_STARTING_SOON: processAuctionStart(auctionId) 호출")
    void consume_AuctionStartingSoon() throws Exception {
        // Given
        String payload = "{}";
        AuctionStartingSoonEvent event = new AuctionStartingSoonEvent(100L);

        when(objectMapper.readValue(payload, AuctionStartingSoonEvent.class)).thenReturn(event);

        // When
        listener.consume(payload, AuctionEventType.AUCTION_STARTING_SOON.name());

        // Then
        verify(watchListService).processAuctionStart(100L);
    }

    // ── AUCTION_ENDING_SOON
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("AUCTION_ENDING_SOON: processAuctionEnd(auctionId) 호출")
    void consume_AuctionEndingSoon() throws Exception {
        // Given
        String payload = "{}";
        AuctionEndingSoonEvent event = new AuctionEndingSoonEvent(200L);

        when(objectMapper.readValue(payload, AuctionEndingSoonEvent.class)).thenReturn(event);

        // When
        listener.consume(payload, AuctionEventType.AUCTION_ENDING_SOON.name());

        // Then
        verify(watchListService).processAuctionEnd(200L);
    }

    // ── 공통 예외/무시 케이스 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eventType이 null이면 아무 처리도 하지 않음")
    void consume_NullEventType_DoesNothing() {
        // When
        listener.consume("{}", null);

        // Then
        verifyNoInteractions(watchListService, objectMapper);
    }

    @Test
    @DisplayName("알 수 없는 이벤트 타입은 warn 로그만 출력하고 서비스 미호출")
    void consume_UnknownEventType_Ignored() {
        // When
        listener.consume("{}", "UNKNOWN_EVENT");

        // Then
        verifyNoInteractions(watchListService);
    }

    @Test
    @DisplayName("WatchList에서 처리하지 않는 이벤트 타입(BID_PLACED)은 무시")
    void consume_UnhandledEventType_Ignored() {
        // When
        listener.consume("{}", AuctionEventType.BID_PLACED.name());

        // Then
        verifyNoInteractions(watchListService);
    }

    @Test
    @DisplayName("JSON 역직렬화 실패 시 RuntimeException 전파")
    void consume_DeserializationFailure_ThrowsRuntimeException() throws Exception {
        // Given
        String payload = "invalid-json";

        when(objectMapper.readValue(payload, AuctionItemCreatedEvent.class))
                .thenThrow(new RuntimeException("parse error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> listener.consume(payload, AuctionEventType.AUCTION_ITEM_CREATED.name()));

        verifyNoInteractions(watchListService);
    }
}
