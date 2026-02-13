package com.fourtune.auction.boundedContext.search.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Table(name = "search_log", indexes = {
        @Index(name = "idx_search_log_keyword", columnList = "keyword"),
        @Index(name = "idx_search_log_user_id", columnList = "user_id"),
        @Index(name = "idx_search_log_created_at", columnList = "created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // null 가능 (비로그인 검색)

    @Column(nullable = false)
    private String keyword;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> categories;

    @Column(nullable = false)
    private BigDecimal minPrice; // null 불가 (최소=0)

    private BigDecimal maxPrice; // null 가능 (상한 없음)

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> status;

    @Column(nullable = false)
    private Integer resultCount;

    @Column(nullable = false)
    private Boolean isSuccess;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public SearchLog(Long userId, String keyword, List<String> categories, BigDecimal minPrice, BigDecimal maxPrice,
            List<String> status, Integer resultCount, Boolean isSuccess) {
        this.userId = userId;
        this.keyword = keyword;
        this.categories = categories;
        this.minPrice = minPrice != null ? minPrice : BigDecimal.ZERO;
        this.maxPrice = maxPrice;
        this.status = status;
        this.resultCount = resultCount;
        this.isSuccess = isSuccess;
    }
}
