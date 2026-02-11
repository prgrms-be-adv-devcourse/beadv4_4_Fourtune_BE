package com.fourtune.auction.boundedContext.fcmToken.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class FcmToken {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private Long userId;

        @Column(nullable = false, unique = true)
        private String token;

        private LocalDateTime lastUsedAt;

        public FcmToken(Long userId, String token) {
            this.userId = userId;
            this.token = token;
            this.lastUsedAt = LocalDateTime.now();
        }

        public void updateLastUsedAt() {
            this.lastUsedAt = LocalDateTime.now();
        }
    }
