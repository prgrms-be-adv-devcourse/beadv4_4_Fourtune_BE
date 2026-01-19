package com.fourtune.auction.boundedContext.payment.domain.entity;

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

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String status;

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
            String role,
            String status
                       ){
        super(id, email, nickname, password, phoneNumber, createdAt, updatedAt, deletedAt);
        this.role = role;
        this.status = status;
    }
}