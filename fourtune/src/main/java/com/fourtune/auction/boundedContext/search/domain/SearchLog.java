package com.fourtune.auction.boundedContext.search.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "search_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // null 가능 (비로그인 검색)

    @Column(nullable = false)
    private String keyword;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public SearchLog(Long userId, String keyword) {
        this.userId = userId;
        this.keyword = keyword;
    }
}
