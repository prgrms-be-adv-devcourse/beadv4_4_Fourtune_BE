package com.fourtune.auction.global.common;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Base Entity
 * 모든 엔티티의 공통 필드
 */
@Getter
public abstract class BaseEntity {
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // TODO: JPA Auditing 설정
    // @CreatedDate
    // @LastModifiedDate
    // @CreatedBy
    // @LastModifiedBy
}

