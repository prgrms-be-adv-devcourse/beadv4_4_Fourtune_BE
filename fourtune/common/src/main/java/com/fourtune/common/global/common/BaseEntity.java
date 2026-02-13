package com.fourtune.common.global.common;

import com.fourtune.common.global.config.GlobalConfig;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base Entity
 * 모든 엔티티의 공통 필드
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    protected void publishEvent(Object event){
        GlobalConfig.getEventPublisher().publish(event);}

    public abstract LocalDateTime getCreatedAt();
    public abstract LocalDateTime getUpdatedAt();

}

