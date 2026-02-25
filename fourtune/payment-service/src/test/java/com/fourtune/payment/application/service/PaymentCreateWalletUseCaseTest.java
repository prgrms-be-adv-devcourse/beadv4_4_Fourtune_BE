package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.domain.entity.PaymentUser;
import com.fourtune.payment.domain.entity.Wallet;
import com.fourtune.payment.port.out.PaymentUserRepository;
import com.fourtune.payment.port.out.WalletRepository;
import com.fourtune.shared.payment.dto.PaymentUserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCreateWalletUseCase 단위 테스트")
class PaymentCreateWalletUseCaseTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private PaymentUserRepository paymentUserRepository;

    @InjectMocks
    private PaymentCreateWalletUseCase sut;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@test.com";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private PaymentUser paymentUser() {
        return new PaymentUser(USER_ID, EMAIL, "nick", "", "", NOW, NOW, null, "ACTIVE");
    }

    private PaymentUserDto dto() {
        return PaymentUserDto.builder()
                .id(USER_ID)
                .email(EMAIL)
                .nickname("nick")
                .createdAt(NOW)
                .updatedAt(NOW)
                .status("ACTIVE")
                .build();
    }

    @Nested
    @DisplayName("createWallet")
    class CreateWallet {

        @Test
        @DisplayName("해당 이메일 유저가 없으면 PAYMENT_USER_NOT_FOUND")
        void userNotFound_throws() {
            when(paymentUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.createWallet(dto()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PAYMENT_USER_NOT_FOUND));

            verify(walletRepository, never()).save(any());
        }

        @Test
        @DisplayName("유저 존재하고 지갑 없으면 새 지갑 생성 후 반환")
        void userExists_noWallet_createsNewWallet() {
            PaymentUser user = paymentUser();
            when(paymentUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(walletRepository.findWalletByPaymentUser(user)).thenReturn(Optional.empty());
            when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> {
                Wallet w = inv.getArgument(0);
                return w;
            });

            Wallet result = sut.createWallet(dto());

            assertThat(result).isNotNull();
            assertThat(result.getPaymentUser()).isSameAs(user);
            assertThat(result.getBalance()).isEqualTo(0L);
            verify(walletRepository).save(any(Wallet.class));
        }

        @Test
        @DisplayName("이미 지갑이 있으면 기존 지갑 반환 (멱등)")
        void userExists_hasWallet_returnsExisting() {
            PaymentUser user = paymentUser();
            Wallet existing = Wallet.builder().paymentUser(user).balance(0L).build();
            when(paymentUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(walletRepository.findWalletByPaymentUser(user)).thenReturn(Optional.of(existing));

            Wallet result = sut.createWallet(dto());

            assertThat(result).isSameAs(existing);
            verify(walletRepository, never()).save(any());
        }
    }
}
