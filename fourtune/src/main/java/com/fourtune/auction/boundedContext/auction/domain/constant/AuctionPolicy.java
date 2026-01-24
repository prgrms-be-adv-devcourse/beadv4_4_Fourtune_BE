package com.fourtune.auction.boundedContext.auction.domain.constant;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "auction")
@Getter
public class AuctionPolicy {
    
    private Integer bidUnit = 1000;
    private Integer autoExtendMinutes = 3;
    private Integer autoExtendThresholdMinutes = 5;
    private Integer paymentDeadlineDays = 3;
    private Integer sellerShippingDeadlineDays = 7;
    private Double settlementFeeRate = 0.1;
    
    public static final BigDecimal MIN_START_PRICE = BigDecimal.valueOf(1000);
    public static final int MIN_AUCTION_DURATION_HOURS = 1;
    public static final int MAX_AUCTION_DURATION_DAYS = 30;
    
    // 자동 연장 관련 상수
    public static final int AUTO_EXTEND_MINUTES = 3;           // 연장 시간 (분)
    public static final int AUTO_EXTEND_THRESHOLD_MINUTES = 5; // 연장 발동 기준 (종료 N분 전)
}
