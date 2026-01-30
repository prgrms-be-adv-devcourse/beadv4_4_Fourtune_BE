package com.fourtune.auction.infra.s3.service;

import com.fourtune.auction.infra.s3.dto.S3PresignedUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        // @Value 주입 시뮬레이션
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");
    }

    @Test
    @DisplayName("S3 Presigned URL 생성 성공 테스트")
    void generatePresignedUrl() throws MalformedURLException {
        // given
        String directory = "auction";
        String fileName = "test.jpg";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/auction/test.jpg?signature=123";

        // Mock 객체 생성
        PresignedPutObjectRequest presignedResponse = org.mockito.Mockito.mock(PresignedPutObjectRequest.class);
        given(presignedResponse.url()).willReturn(java.net.URI.create(expectedUrl).toURL());

        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedResponse);

        // when
        S3PresignedUrlResponse response = s3Service.generatePresignedUrl(directory, fileName);

        // then
        assertThat(response).isNotNull();
        assertThat(response.presignedUrl()).isEqualTo(expectedUrl);
        assertThat(response.imageUrl()).contains("test-bucket");
        assertThat(response.imageUrl()).contains(directory);

        verify(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
    }
}
