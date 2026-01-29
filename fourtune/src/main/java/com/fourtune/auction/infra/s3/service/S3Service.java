package com.fourtune.auction.infra.s3.service;

import com.fourtune.auction.infra.s3.dto.S3PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * Presigned URL 생성
     * 
     * @param directory        디렉토리 명 (예: auction, profile)
     * @param originalFileName 원본 파일명
     * @return Presigned URL 및 실제 접근 URL
     */
    public S3PresignedUrlResponse generatePresignedUrl(String directory, String originalFileName) {
        String fileName = createFileName(directory, originalFileName);

        // Presigned URL 요청 생성 (PUT 메서드용)
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // 10분간 유효
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();
        String imageUrl = "https://" + bucket + ".s3.amazonaws.com/" + fileName; // 기본 S3 URL (CloudFront 사용 시 변경 필요)

        log.info("Presigned URL generated: {}", presignedUrl);

        return new S3PresignedUrlResponse(presignedUrl, imageUrl);
    }

    private String createFileName(String directory, String originalFileName) {
        String ext = extractExtension(originalFileName);
        // UUID로 파일명 중복 방지
        return directory + "/" + UUID.randomUUID().toString() + "." + ext;
    }

    private String extractExtension(String originalFileName) {
        try {
            return originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "jpg"; // 확장자가 없는 경우 기본값
        }
    }
}
