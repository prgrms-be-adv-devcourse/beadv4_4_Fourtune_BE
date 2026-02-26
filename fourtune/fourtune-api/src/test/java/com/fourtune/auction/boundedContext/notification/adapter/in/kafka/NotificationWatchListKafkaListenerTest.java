package com.fourtune.auction.boundedContext.notification.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationWatchListKafkaListenerTest {

    @Mock
    private NotificationFacade notificationFacade;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationWatchListKafkaListener listener;

    @Test
    @DisplayName("WATCHLIST_AUCTION_STARTED 이벤트 수신 시 WATCHLIST_START 그룹 알림 생성")
    void consume_WatchlistAuctionStarted() throws Exception {
        // Given
        String payload = "{\"auctionItemId\":1,\"users\":[10,20,30],\"auctionTitle\":\"테스트 상품\"}";
        String eventType = WatchListEventType.WATCHLIST_AUCTION_STARTED.name();
        ObjectMapper realMapper = new ObjectMapper();
        JsonNode node = realMapper.readTree(payload);

        when(objectMapper.readTree(payload)).thenReturn(node);

        // When
        listener.consume(payload, eventType);

        // Then
        verify(notificationFacade).createGroupNotification(
                argThat(users -> users.containsAll(List.of(10L, 20L, 30L)) && users.size() == 3),
                eq(1L),
                eq(NotificationType.WATCHLIST_START),
                eq("테스트 상품"));
    }

    @Test
    @DisplayName("WATCHLIST_AUCTION_ENDED 이벤트 수신 시 WATCHLIST_END 그룹 알림 생성")
    void consume_WatchlistAuctionEnded() throws Exception {
        // Given
        String payload = "{\"auctionItemId\":2,\"users\":[11,22],\"auctionTitle\":\"종료 상품\"}";
        String eventType = WatchListEventType.WATCHLIST_AUCTION_ENDED.name();
        ObjectMapper realMapper = new ObjectMapper();
        JsonNode node = realMapper.readTree(payload);

        when(objectMapper.readTree(payload)).thenReturn(node);

        // When
        listener.consume(payload, eventType);

        // Then
        verify(notificationFacade).createGroupNotification(
                argThat(users -> users.containsAll(List.of(11L, 22L)) && users.size() == 2),
                eq(2L),
                eq(NotificationType.WATCHLIST_END),
                eq("종료 상품"));
    }

    @Test
    @DisplayName("eventType이 null이면 아무 처리도 하지 않음")
    void consume_NullEventType_DoesNothing() {
        // When
        listener.consume("{}", null);

        // Then
        verifyNoInteractions(notificationFacade, objectMapper);
    }

    @Test
    @DisplayName("알 수 없는 이벤트 타입은 warn 로그만 출력하고 서비스 미호출")
    void consume_UnknownEventType_IgnoresAndNoInteraction() {
        // When
        listener.consume("{}", "UNKNOWN_EVENT");

        // Then
        verifyNoInteractions(notificationFacade);
    }

    @Test
    @DisplayName("JSON 역직렬화 실패 시 RuntimeException 전파")
    void consume_DeserializationFailure_ThrowsRuntimeException() throws Exception {
        // Given
        String payload = "invalid-json";
        String eventType = WatchListEventType.WATCHLIST_AUCTION_STARTED.name();

        when(objectMapper.readTree(payload)).thenThrow(new RuntimeException("parse error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> listener.consume(payload, eventType));
        verifyNoInteractions(notificationFacade);
    }
}
