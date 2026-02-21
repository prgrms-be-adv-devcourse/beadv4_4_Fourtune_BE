package com.fourtune.auction.boundedContext.auction.domain.constant;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Component
@ConfigurationProperties(prefix = "bid.policy")
public class BidPolicy {
    
    /**
     * 기본 입찰 단위 (원)
     */
    private Integer defaultBidUnit = 1000;
    
    /**
     * 최소 입찰 금액 (원)
     */
    private BigDecimal minimumBidAmount = new BigDecimal("1000");
    
    /**
     * 최대 입찰 금액 (원)
     */
    private BigDecimal maximumBidAmount = new BigDecimal("1000000000");
    
    /**
     * 자동 연장 트리거 시간 (분)
     * 경매 종료 N분 전에 입찰이 들어오면 자동 연장
     */
    private Integer autoExtendTriggerMinutes = 5;
    
    /**
     * 자동 연장 시간 (분)
     */
    private Integer autoExtendDurationMinutes = 3;
    
    /**
     * 최대 자동 연장 횟수
     */
    private Integer maxAutoExtendCount = 5;
    
    /**
     * 입찰 취소 가능 시간 (분)
     * 입찰 후 N분 이내에만 취소 가능
     */
    private Integer bidCancellableMinutes = 5;
    
    /**
     * 보증금 비율 (%)
     * 입찰 시 입찰가의 N%를 보증금으로 예치
     */
    private BigDecimal depositRate = new BigDecimal("10");
    
    // Setter methods for ConfigurationProperties
    public void setDefaultBidUnit(Integer defaultBidUnit) {
        this.defaultBidUnit = defaultBidUnit;
    }
    
    public void setMinimumBidAmount(BigDecimal minimumBidAmount) {
        this.minimumBidAmount = minimumBidAmount;
    }
    
    public void setMaximumBidAmount(BigDecimal maximumBidAmount) {
        this.maximumBidAmount = maximumBidAmount;
    }
    
    public void setAutoExtendTriggerMinutes(Integer autoExtendTriggerMinutes) {
        this.autoExtendTriggerMinutes = autoExtendTriggerMinutes;
    }
    
    public void setAutoExtendDurationMinutes(Integer autoExtendDurationMinutes) {
        this.autoExtendDurationMinutes = autoExtendDurationMinutes;
    }
    
    public void setMaxAutoExtendCount(Integer maxAutoExtendCount) {
        this.maxAutoExtendCount = maxAutoExtendCount;
    }
    
    public void setBidCancellableMinutes(Integer bidCancellableMinutes) {
        this.bidCancellableMinutes = bidCancellableMinutes;
    }
    
    public void setDepositRate(BigDecimal depositRate) {
        this.depositRate = depositRate;
    }
}
