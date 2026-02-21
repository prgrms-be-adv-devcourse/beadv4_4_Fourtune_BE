package com.fourtune.auction.boundedContext.notification.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationAuctionKafkaListenerTest {

    @Mock
    private NotificationFacade notificationFacade;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationAuctionKafkaListener listener;

    // ── BID_PLACED ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("BID_PLACED: 이전 입찰자 있을 때 판매자 알림 + OUTBID 알림 모두 발송")
    void consume_BidPlaced_WithPreviousBidder() throws Exception {
        String payload = "{}";
        BidPlacedEvent event = new BidPlacedEvent(
                1L, 100L, "경매상품", 10L, 20L, 15L,
                BigDecimal.valueOf(5000), LocalDateTime.now());

        when(objectMapper.readValue(payload, BidPlacedEvent.class)).thenReturn(event);

        listener.consume(payload, AuctionEventType.BID_PLACED.name());

        verify(notificationFacade).bidPlaceToSeller(10L, 20L, 100L, NotificationType.BID_RECEIVED);
        verify(notificationFacade).createNotification(15L, 100L, NotificationType.OUTBID);
    }

    @Test
    @DisplayName("BID_PLACED: 이전 입찰자 없을 때 OUTBID 알림 미발송")
    void consume_BidPlaced_WithoutPreviousBidder() throws Exception {
        String payload = "{}";
        BidPlacedEvent event = new BidPlacedEvent(
                1L, 100L, "경매상품", 10L, 20L, null,
                BigDecimal.valueOf(5000), LocalDateTime.now());

        when(objectMapper.readValue(payload, BidPlacedEvent.class)).thenReturn(event);

        listener.consume(payload, AuctionEventType.BID_PLACED.name());

        verify(notificationFacade).bidPlaceToSeller(10L, 20L, 100L, NotificationType.BID_RECEIVED);
        verify(notificationFacade, never()).createNotification(any(), any(), eq(NotificationType.OUTBID));
    }

    // ── AUCTION_CLOSED ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUCTION_CLOSED: 낙찰자 있을 때 판매자·낙찰자 모두 AUCTION_SUCCESS 알림")
    void consume_AuctionClosed_WithWinner() throws Exception {
        String payload = "{}";
        AuctionClosedEvent event = new AuctionClosedEvent(
                100L, "경매상품", 10L, 20L, BigDecimal.valueOf(10000), "ORD-001");

        when(objectMapper.readValue(payload, AuctionClosedEvent.class)).thenReturn(event);

        listener.consume(payload, AuctionEventType.AUCTION_CLOSED.name());

        verify(notificationFacade).createNotification(20L, 100L, NotificationType.AUCTION_SUCCESS);
        verify(notificationFacade).createNotification(10L, 100L, NotificationType.AUCTION_SUCCESS);
    }

    @Test
    @DisplayName("AUCTION_CLOSED: 낙찰자 없을 때 판매자에게 AUCTION_FAILED 알림")
    void consume_AuctionClosed_WithoutWinner() throws Exception {
        String payload = "{}";
        AuctionClosedEvent event = new AuctionClosedEvent(
                100L, "경매상품", 10L, null, null, null);

        when(objectMapper.readValue(payload, AuctionClosedEvent.class)).thenReturn(event);

        listener.consume(payload, AuctionEventType.AUCTION_CLOSED.name());

        verify(notificationFacade).createNotification(10L, 100L, NotificationType.AUCTION_FAILED);
        verify(notificationFacade, never()).createNotification(any(), any(), eq(NotificationType.AUCTION_SUCCESS));
    }

    // ── AUCTION_BUY_NOW ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUCTION_BUY_NOW: 구매자·판매자 모두 AUCTION_SUCCESS 알림")
    void consume_AuctionBuyNow() throws Exception {
        String payload = "{}";
        AuctionBuyNowEvent event = new AuctionBuyNowEvent(
                100L, 10L, 30L, BigDecimal.valueOf(20000), "ORD-002", LocalDateTime.now());

        when(objectMapper.readValue(payload, AuctionBuyNowEvent.class)).thenReturn(event);

        listener.consume(payload, AuctionEventType.AUCTION_BUY_NOW.name());

        verify(notificationFacade).createNotification(30L, 100L, NotificationType.AUCTION_SUCCESS);
        verify(notificationFacade).createNotification(10L, 100L, NotificationType.AUCTION_SUCCESS);
    }

    // ── AUCTION_EXTENDED ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUCTION_EXTENDED: 이벤트 수신해도 현재 알림 발송 없음 (TODO)")
    void consume_AuctionExtended_NoNotification() throws Exception {
        String payload = "{}";
        AuctionExtendedEvent event = new AuctionExtendedEvent(100L, LocalDateTime.now().plusHours(1));

        when(objectMapper.readValue(payload, AuctionExtendedEvent.class)).thenReturn(event);

        listener.consume(payload, AuctionEventType.AUCTION_EXTENDED.name());

        verifyNoInteractions(notificationFacade);
    }

    // ── BID_CANCELED ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("BID_CANCELED: 판매자에게 BID_RECEIVED 알림 발송")
    void consume_BidCanceled() throws Exception {
        String payload = "{}";
        BidCanceledEvent event = new BidCanceledEvent(
                1L, 100L, "경매상품", 10L, 20L,
                BigDecimal.valueOf(5000), LocalDateTime.now());

        when(objectMapper.readValue(payload, BidCanceledEvent.class)).thenReturn(event);

        listener.consume(payload, AuctionEventType.BID_CANCELED.name());

        verify(notificationFacade).createNotification(10L, 100L, NotificationType.BID_CANCELED);
    }

    // ── 공통 예외/무시 케이스 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("eventType이 null이면 아무 처리도 하지 않음")
    void consume_NullEventType_DoesNothing() {
        listener.consume("{}", null);
        verifyNoInteractions(notificationFacade, objectMapper);
    }

    @Test
    @DisplayName("알 수 없는 이벤트 타입은 서비스 미호출")
    void consume_UnknownEventType_Ignored() {
        listener.consume("{}", "UNKNOWN_EVENT");
        verifyNoInteractions(notificationFacade);
    }

    @Test
    @DisplayName("알림 도메인에서 처리하지 않는 이벤트 타입(AUCTION_ITEM_CREATED)은 무시")
    void consume_UnhandledAuctionEventType_Ignored() {
        listener.consume("{}", AuctionEventType.AUCTION_ITEM_CREATED.name());
        verifyNoInteractions(notificationFacade);
    }

    @Test
    @DisplayName("JSON 역직렬화 실패 시 RuntimeException 전파")
    void consume_DeserializationFailure_ThrowsRuntimeException() throws Exception {
        String payload = "invalid-json";

        when(objectMapper.readValue(payload, BidPlacedEvent.class))
                .thenThrow(new RuntimeException("parse error"));

        assertThrows(RuntimeException.class,
                () -> listener.consume(payload, AuctionEventType.BID_PLACED.name()));
        verifyNoInteractions(notificationFacade);
    }
}
