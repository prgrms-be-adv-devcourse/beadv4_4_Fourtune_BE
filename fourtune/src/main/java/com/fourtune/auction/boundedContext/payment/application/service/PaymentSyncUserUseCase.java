package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.shared.payment.event.PaymentUserCreatedEvent;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.payment.dto.PaymentUserDto;
import com.fourtune.auction.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentSyncUserUseCase {
    private final PaymentUserRepository paymentUserRepository;
    private final EventPublisher eventPublisher;

    public PaymentUser syncUser(UserResponse user) {

        boolean isNew = !paymentUserRepository.existsById(user.id());

        // 같은 id 유저가 있을 경우 업데이트 되어 저장
        PaymentUser paymentUser = paymentUserRepository.save(
                new PaymentUser(
                        user.id(),
                        user.email(),
                        user.nickname(),
                        "",
                        "",
                        user.createdAt(),
                        user.updatedAt(),
                        null,
                        user.status()
                )
        );

        if (isNew) {
            eventPublisher.publish(
                    new PaymentUserCreatedEvent(
                            paymentUser.toDto()
                    )
            );
        }

        return paymentUser;
    }
}
