package com.fourtune.payment.application.service;

import com.fourtune.payment.domain.entity.PaymentUser;
import com.fourtune.payment.port.out.PaymentUserRepository;
import com.fourtune.shared.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentSyncUserUseCase 단위 테스트")
class PaymentSyncUserUseCaseTest {

    @Mock
    private PaymentUserRepository paymentUserRepository;
    @Mock
    private PaymentCreateWalletUseCase paymentCreateWalletUseCase;

    @InjectMocks
    private PaymentSyncUserUseCase sut;

    private static final Long USER_ID = 100L;
    private static final String EMAIL = "user@test.com";
    private static final String NICKNAME = "테스트유저";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private UserResponse userResponse() {
        return new UserResponse(USER_ID, NOW, NOW, EMAIL, NICKNAME, "ACTIVE", "USER");
    }

    private PaymentUser savedPaymentUser() {
        return new PaymentUser(USER_ID, EMAIL, NICKNAME, "", "", NOW, NOW, null, "ACTIVE");
    }

    @Nested
    @DisplayName("syncUser")
    class SyncUser {

        @Test
        @DisplayName("신규 유저면 PaymentUser 저장 후 createWallet 호출")
        void newUser_createsUserAndWallet() {
            when(paymentUserRepository.existsById(USER_ID)).thenReturn(false);
            when(paymentUserRepository.save(any(PaymentUser.class))).thenAnswer(inv -> inv.getArgument(0));

            PaymentUser result = sut.syncUser(userResponse());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(USER_ID);
            assertThat(result.getEmail()).isEqualTo(EMAIL);
            assertThat(result.getNickname()).isEqualTo(NICKNAME);

            verify(paymentUserRepository).existsById(USER_ID);
            verify(paymentUserRepository).save(any(PaymentUser.class));

            ArgumentCaptor<com.fourtune.shared.payment.dto.PaymentUserDto> dtoCaptor =
                    ArgumentCaptor.forClass(com.fourtune.shared.payment.dto.PaymentUserDto.class);
            verify(paymentCreateWalletUseCase).createWallet(dtoCaptor.capture());
            assertThat(dtoCaptor.getValue().getEmail()).isEqualTo(EMAIL);
            assertThat(dtoCaptor.getValue().getId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("기존 유저면 PaymentUser만 저장하고 createWallet 호출 안 함")
        void existingUser_updatesUserOnly_noWalletCreation() {
            when(paymentUserRepository.existsById(USER_ID)).thenReturn(true);
            when(paymentUserRepository.save(any(PaymentUser.class))).thenAnswer(inv -> inv.getArgument(0));

            PaymentUser result = sut.syncUser(userResponse());

            assertThat(result).isNotNull();
            verify(paymentUserRepository).existsById(USER_ID);
            verify(paymentUserRepository).save(any(PaymentUser.class));
            verify(paymentCreateWalletUseCase, never()).createWallet(any());
        }
    }
}
