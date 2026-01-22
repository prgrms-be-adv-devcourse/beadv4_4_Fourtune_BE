package com.fourtune.auction.boundedContext.payment.domain.entity;

import com.fourtune.auction.shared.payment.dto.PaymentUserDto;
import com.fourtune.auction.shared.user.domain.ReplicaUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT_USER")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentUser extends ReplicaUser {

   @Builder
    public PaymentUser(
            Long id,
            String email,
            String nickname,
            String password,
            String phoneNumber,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            String status){
        super(id, email, nickname, password, phoneNumber, createdAt, updatedAt, deletedAt, status);
    }

    public PaymentUserDto toDto(){
        return PaymentUserDto.builder()
                .id(this.getId())
                .email(this.getEmail())
                .nickname(this.getNickname())
                .password(this.getPassword())
                .phoneNumber(this.getPhoneNumber())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .deletedAt(this.getDeletedAt())
                .status(this.getStatus())
                .build();
    }
}
