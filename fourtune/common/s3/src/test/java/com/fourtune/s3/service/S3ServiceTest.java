package com.fourtune.s3.service;

import com.fourtune.s3.dto.S3PresignedUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
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
        ReflectionTestUtils.setField(s3Service, "pathPrefix", "test/local/tester");
        ReflectionTestUtils.setField(s3Service, "publicUrl", "https://test-bucket.s3.amazonaws.com");
    }

    @Test
    @DisplayName("S3 Presigned URL 생성 성공 테스트 - 디렉토리 포함")
    void generatePresignedUrl() throws MalformedURLException {
        // given
        String directory = "auction";
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/auction/test.jpg?signature=123";

        // Mock 객체 생성
        PresignedPutObjectRequest presignedResponse = org.mockito.Mockito.mock(PresignedPutObjectRequest.class);
        given(presignedResponse.url()).willReturn(java.net.URI.create(expectedUrl).toURL());

        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedResponse);

        // when
        S3PresignedUrlResponse response = s3Service.generatePresignedUrl(directory, fileName, contentType);

        // then
        assertThat(response).isNotNull();
        assertThat(response.presignedUrl()).isEqualTo(expectedUrl);
        // [검증] 디렉토리가 포함된 경로인지 확인
        assertThat(response.imageUrl()).contains("test/local/tester/" + directory);

        // Content-Type 검증
        verify(s3Presigner).presignPutObject(org.mockito.ArgumentMatchers.argThat((PutObjectPresignRequest request) -> {
            return request.putObjectRequest().contentType().equals(contentType);
        }));
    }

    @Test
    @DisplayName("S3 Presigned URL 생성 성공 테스트 - 디렉토리 없음(null/empty) -> 바로 하위 저장")
    void generatePresignedUrl_withBlankDirectory() throws MalformedURLException {
        // given
        String directory = ""; // or null
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        String expectedUrl = "https://s3.url";

        PresignedPutObjectRequest presignedResponse = org.mockito.Mockito.mock(PresignedPutObjectRequest.class);
        given(presignedResponse.url()).willReturn(java.net.URI.create(expectedUrl).toURL());

        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedResponse);

        // when
        S3PresignedUrlResponse response = s3Service.generatePresignedUrl(directory, fileName, contentType);

        // then
        // [검증] publicUrl + "/" + key 형태의 완전한 URL이 반환되는지 확인
        // 예: https://test-bucket.s3.amazonaws.com/test/local/tester/[UUID].jpg
        assertThat(response.imageUrl()).startsWith("https://test-bucket.s3.amazonaws.com/");
        assertThat(response.imageUrl()).contains("test/local/tester/");

        System.out.println("Generated Key: " + response.imageUrl());
    }

    @Test
    @DisplayName("S3 Presigned URL 생성 - 확장자 없는 경우 기본값(jpg) 사용")
    void generatePresignedUrl_defaultExtension() throws MalformedURLException {
        // given
        String directory = "auction";
        String fileName = "testFileWithoutExt";
        String contentType = "image/jpeg";
        String expectedUrl = "https://s3.url"; // Mock URL

        PresignedPutObjectRequest presignedResponse = org.mockito.Mockito.mock(PresignedPutObjectRequest.class);
        given(presignedResponse.url()).willReturn(java.net.URI.create(expectedUrl).toURL());

        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedResponse);

        // when
        S3PresignedUrlResponse response = s3Service.generatePresignedUrl(directory, fileName, contentType);

        // then
        assertThat(response.imageUrl()).endsWith(".jpg");
    }

    @Test
    @DisplayName("S3 Presigned GET URL 생성 성공 테스트")
    void generatePresignedGetUrl() throws MalformedURLException {
        // given
        String fileKey = "auction/test.jpg";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/auction/test.jpg?signature=456";

        // Mock 객체 생성
        PresignedGetObjectRequest presignedResponse = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);
        given(presignedResponse.url()).willReturn(java.net.URI.create(expectedUrl).toURL());

        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(presignedResponse);

        // when
        String resultUrl = s3Service.generatePresignedGetUrl(fileKey);

        // then
        assertThat(resultUrl).isEqualTo(expectedUrl);

        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("S3 Presigned GET URL 생성 실패 (예외 발생) -> null 반환")
    void generatePresignedGetUrl_exception() {
        // given
        String fileKey = "auction/error.jpg";

        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willThrow(new RuntimeException("S3 Error"));

        // when
        String resultUrl = s3Service.generatePresignedGetUrl(fileKey);

        // then
        assertThat(resultUrl).isNull();
    }

    @Test
    @DisplayName("S3 Presigned URL 생성 - 파일명이 null인 경우 기본값(jpg) 사용")
    void generatePresignedUrl_nullFileName() throws MalformedURLException {
        // given
        String directory = "auction";
        String fileName = null;
        String contentType = "image/jpeg";
        String expectedUrl = "https://s3.url";

        PresignedPutObjectRequest presignedResponse = org.mockito.Mockito.mock(PresignedPutObjectRequest.class);
        given(presignedResponse.url()).willReturn(java.net.URI.create(expectedUrl).toURL());

        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedResponse);

        // when
        S3PresignedUrlResponse response = s3Service.generatePresignedUrl(directory, fileName, contentType);

        // then
        assertThat(response.imageUrl()).endsWith(".jpg");
    }

    @Test
    @DisplayName("S3 Presigned GET URL 생성 - 키가 null이거나 빈 경우 null 반환")
    void generatePresignedGetUrl_invalidKey() {
        // when
        String resultNull = s3Service.generatePresignedGetUrl(null);
        String resultEmpty = s3Service.generatePresignedGetUrl("");
        String resultBlank = s3Service.generatePresignedGetUrl("   ");

        // then
        assertThat(resultNull).isNull();
        assertThat(resultEmpty).isNull();
        assertThat(resultBlank).isNull();
    }
}
