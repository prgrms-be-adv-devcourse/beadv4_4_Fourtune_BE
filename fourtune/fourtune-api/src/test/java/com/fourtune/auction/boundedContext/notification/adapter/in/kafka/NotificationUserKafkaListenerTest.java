package com.fourtune.auction.boundedContext.notification.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.notification.application.NotificationFacade;
import com.fourtune.auction.boundedContext.notification.application.NotificationSettingsService;
import com.fourtune.shared.user.event.UserEventType;
import com.fourtune.shared.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationUserKafkaListenerTest {

    @Mock
    private NotificationFacade notificationFacade;

    @Mock
    private NotificationSettingsService notificationSettingsService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private NotificationUserKafkaListener listener;

    @Test
    @DisplayName("USER_JOINED 이벤트 수신 시 syncUser + createNotificationSettings 호출")
    void handleUserEvent_UserJoined() throws Exception {
        // Given
        String payload = "{\"id\":1,\"email\":\"test@test.com\",\"nickname\":\"tester\",\"status\":\"ACTIVE\",\"role\":\"USER\"}";
        String eventType = UserEventType.USER_JOINED.name();
        UserResponse user = UserResponse.builder().id(1L).email("test@test.com").build();

        when(objectMapper.readValue(payload, UserResponse.class)).thenReturn(user);

        // When
        listener.handleUserEvent(payload, eventType, ack);

        // Then
        verify(notificationFacade).syncUser(user);
        verify(notificationSettingsService).createNotificationSettings(user);
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("USER_MODIFIED 이벤트 수신 시 syncUser만 호출")
    void handleUserEvent_UserModified() throws Exception {
        // Given
        String payload = "{\"id\":1,\"nickname\":\"modified\"}";
        String eventType = UserEventType.USER_MODIFIED.name();
        UserResponse user = UserResponse.builder().id(1L).build();

        when(objectMapper.readValue(payload, UserResponse.class)).thenReturn(user);

        // When
        listener.handleUserEvent(payload, eventType, ack);

        // Then
        verify(notificationFacade).syncUser(user);
        verify(notificationSettingsService, never()).createNotificationSettings(any());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("USER_DELETED 이벤트 수신 시 syncUser 호출")
    void handleUserEvent_UserDeleted() throws Exception {
        // Given
        String payload = "{\"id\":1}";
        String eventType = UserEventType.USER_DELETED.name();
        UserResponse user = UserResponse.builder().id(1L).build();

        when(objectMapper.readValue(payload, UserResponse.class)).thenReturn(user);

        // When
        listener.handleUserEvent(payload, eventType, ack);

        // Then
        verify(notificationFacade).syncUser(user);
        verify(notificationSettingsService, never()).createNotificationSettings(any());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("JSON 역직렬화 실패 시 RuntimeException 발생 및 ack 미호출")
    void handleUserEvent_DeserializationFailure_ThrowsRuntimeException() throws Exception {
        // Given
        String payload = "invalid-json";
        String eventType = UserEventType.USER_JOINED.name();

        when(objectMapper.readValue(payload, UserResponse.class))
                .thenThrow(new RuntimeException("JSON parse error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> listener.handleUserEvent(payload, eventType, ack));

        verify(ack, never()).acknowledge();
        verifyNoInteractions(notificationFacade);
    }

    @Test
    @DisplayName("알 수 없는 이벤트 타입은 IllegalArgumentException -> RuntimeException으로 전파")
    void handleUserEvent_UnknownEventType_ThrowsRuntimeException() throws Exception {
        // Given
        String payload = "{\"id\":1}";
        String eventType = "UNKNOWN_EVENT";
        UserResponse user = UserResponse.builder().id(1L).build();

        when(objectMapper.readValue(payload, UserResponse.class)).thenReturn(user);

        // When & Then
        assertThrows(RuntimeException.class,
                () -> listener.handleUserEvent(payload, eventType, ack));

        verify(ack, never()).acknowledge();
        verifyNoInteractions(notificationFacade);
    }
}
