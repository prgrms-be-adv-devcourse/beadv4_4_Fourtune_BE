package com.fourtune.auction.boundedContext.search.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.search.application.service.AuctionItemIndexingHandler;
import com.fourtune.auction.boundedContext.search.domain.SearchAuctionItemView;
import com.fourtune.shared.auction.event.AuctionItemCreatedEvent;
import com.fourtune.shared.auction.event.AuctionItemDeletedEvent;
import com.fourtune.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionItemSearchKafkaListenerTest {

    @Mock
    private AuctionItemIndexingHandler indexingHandler;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuctionItemSearchKafkaListener listener;

    @Test
    @DisplayName("생성 이벤트(AUCTION_ITEM_CREATED) 수신 시 인덱스 추가/업데이트")
    void consume_CreatedEvent() throws Exception {
        // Given
        String payload = "{\"auctionItemId\": 1, \"title\": \"Test Item\"}";
        String eventType = AuctionEventType.AUCTION_ITEM_CREATED.name();
        AuctionItemCreatedEvent event = mock(AuctionItemCreatedEvent.class);

        when(objectMapper.readValue(payload, AuctionItemCreatedEvent.class)).thenReturn(event);
        when(event.auctionItemId()).thenReturn(1L);

        // When
        listener.consume(payload, eventType);

        // Then
        verify(indexingHandler).upsert(any(SearchAuctionItemView.class));
    }

    @Test
    @DisplayName("수정 이벤트(AUCTION_ITEM_UPDATED) 수신 시 인덱스 업데이트")
    void consume_UpdatedEvent() throws Exception {
        // Given
        String payload = "{\"auctionItemId\": 1, \"title\": \"Updated Item\"}";
        String eventType = AuctionEventType.AUCTION_ITEM_UPDATED.name();
        AuctionItemUpdatedEvent event = mock(AuctionItemUpdatedEvent.class);

        when(objectMapper.readValue(payload, AuctionItemUpdatedEvent.class)).thenReturn(event);
        when(event.auctionItemId()).thenReturn(1L);

        // When
        listener.consume(payload, eventType);

        // Then
        verify(indexingHandler).upsert(any(SearchAuctionItemView.class));
    }

    @Test
    @DisplayName("삭제 이벤트(AUCTION_ITEM_DELETED) 수신 시 인덱스 삭제")
    void consume_DeletedEvent() throws Exception {
        // Given
        String payload = "{\"auctionItemId\": 1}";
        String eventType = AuctionEventType.AUCTION_ITEM_DELETED.name();
        AuctionItemDeletedEvent event = mock(AuctionItemDeletedEvent.class);

        when(objectMapper.readValue(payload, AuctionItemDeletedEvent.class)).thenReturn(event);
        when(event.auctionItemId()).thenReturn(1L);

        // When
        listener.consume(payload, eventType);

        // Then
        verify(indexingHandler).delete(1L);
    }

    @Test
    @DisplayName("알 수 없는 이벤트 타입은 무시")
    void consume_UnknownEvent() {
        // Given
        String payload = "{}";
        String eventType = "UNKNOWN_EVENT";

        // When
        listener.consume(payload, eventType);

        // Then
        verifyNoInteractions(indexingHandler);
    }
}
