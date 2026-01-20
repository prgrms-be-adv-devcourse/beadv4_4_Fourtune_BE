package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.shared.payment.event.PaymentUserCreatedEvent;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.payment.dto.PaymentUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncUserUseCase {
    private final PaymentUserRepository paymentUserRepository;
    private final EventPublisher eventPublisher;

    public PaymentUser syncUser(PaymentUserDto dto) {

        boolean isNew = !paymentUserRepository.existsById(dto.getId());

        // 같은 id 유저가 있을 경우 업데이트 되어 저장
        PaymentUser user = paymentUserRepository.save(
                new PaymentUser(
                        dto.getId(),
                        dto.getEmail(),
                        dto.getNickname(),
                        "",
                        dto.getPhoneNumber(),
                        dto.getCreatedAt(),
                        dto.getUpdatedAt(),
                        dto.getDeletedAt(),
                        dto.getRole(),
                        dto.getStatus()
                )
        );


        if (isNew) {
            eventPublisher.publish(
                    new PaymentUserCreatedEvent(
                            user.toDto()
                    )
            );
        }

        return user;
    }
}