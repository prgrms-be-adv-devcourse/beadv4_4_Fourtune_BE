package com.fourtune.auction.global.common;

import com.fourtune.auction.global.config.GlobalConfig;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Base Entity
 * 모든 엔티티의 공통 필드
 */
@Getter
@MappedSuperclass
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    protected void publishEvent(Object event){
        GlobalConfig.getEventPublisher().publish(event);}

    public abstract LocalDateTime getCreatedAt();
    public abstract LocalDateTime getUpdatedAt();

}

