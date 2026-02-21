package com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Document(indexName = "auction_items")
// @Setting(settingPath = "...")
public class SearchAuctionItemDocument {

    @Id
    private Long auctionItemId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category; // enum 이름

    @Field(type = FieldType.Keyword)
    private String status; // enum 이름

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100) // BigDecimal 정렬용
    private BigDecimal startPrice;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal currentPrice;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal buyNowPrice;

    @Field(type = FieldType.Boolean)
    private Boolean buyNowEnabled;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private ZonedDateTime startAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private ZonedDateTime endAt;

    @Field(type = FieldType.Keyword, index = false)
    private String thumbnailUrl;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private ZonedDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private ZonedDateTime updatedAt;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Integer)
    private Integer watchlistCount;

    @Field(type = FieldType.Integer)
    private Integer bidCount;

    @Field(type = FieldType.Long)
    private Long sellerId;

    @Field(type = FieldType.Keyword)
    private String sellerName;
}
