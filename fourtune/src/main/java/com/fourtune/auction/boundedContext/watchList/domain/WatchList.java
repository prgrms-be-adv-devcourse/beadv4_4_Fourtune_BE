package com.fourtune.auction.boundedContext.watchList.domain;

import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "watch_list")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchList extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long auctionItemId;

    private boolean isStartAlertSent;
    private boolean isEndAlertSent;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public WatchList(Long userId, Long auctionItemId) {
        this.userId = userId;
        this.auctionItemId = auctionItemId;
        this.isStartAlertSent = false;
        this.isEndAlertSent = false;
    }

    public void markStartAlertSent() {
        this.isStartAlertSent = true;
    }

    public void markEndAlertSent() {
        this.isEndAlertSent = true;
    }

}
