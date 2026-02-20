package com.fourtune.auction;

import com.fourtune.auction.adapter.out.api.UserClient;
import com.fourtune.common.shared.auction.kafka.AuctionKafkaProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fourtune.security.handler.OAuth2SuccessHandler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * auction-service 컨텍스트 로드 검증.
 * - Feign UserClient는 @MockitoBean으로 대체해 실제 API 호출 없이 기동.
 * - common SecurityConfig가 요구하는 OAuth2 빈도 Mock.
 * - test profile: H2, Kafka/Redis/S3 autoconfigure 제외, JWT 설정 포함.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuctionServiceApplicationTests {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private AuctionKafkaProducer auctionKafkaProducer;

    @Test
    @DisplayName("Spring 컨텍스트가 정상 기동한다")
    void contextLoads() {
        assertThat(userClient).isNotNull();
    }
}
