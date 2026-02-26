package com.fourtune.s3.service;

import com.fourtune.s3.dto.S3PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.s3.path-prefix}")
    private String pathPrefix;

    // 브라우저에서 접근할 공개 URL 베이스 (예: http://localhost:9000 또는 https://bucket.s3.amazonaws.com)
    @Value("${spring.cloud.aws.s3.public-url}")
    private String publicUrl;

    /**
     * 서버에서 직접 S3/MinIO에 파일 업로드
     * - 업로드 후 공개 접근 가능한 URL 반환
     *
     * @param file      업로드할 파일
     * @param directory 저장 디렉토리 (예: "auctions")
     * @return 공개 접근 URL
     */
    public String uploadFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String key = createFileName(directory, file.getOriginalFilename());

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = publicUrl + "/" + bucket + "/" + key;
            log.info("파일 업로드 완료: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Presigned URL 생성 (업로드용 - 클라이언트가 직접 S3에 업로드할 때 사용)
     */
    public S3PresignedUrlResponse generatePresignedUrl(String directory, String originalFileName, String contentType) {
        String fileName = createFileName(directory, originalFileName);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        log.info("Presigned PUT URL generated: {}", presignedUrl);
        return new S3PresignedUrlResponse(presignedUrl, fileName);
    }

    /**
     * 조회용 Presigned URL 생성 (Private 버킷에서 사용)
     */
    public String generatePresignedGetUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }

        try {
            var objectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();

            var presignRequest = software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(objectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Presigned GET URL 생성 실패: {}", fileKey, e);
            return null;
        }
    }

    private String createFileName(String directory, String originalFileName) {
        String ext = extractExtension(originalFileName);
        if (directory == null || directory.isBlank()) {
            return pathPrefix + "/" + UUID.randomUUID() + "." + ext;
        }
        return pathPrefix + "/" + directory + "/" + UUID.randomUUID() + "." + ext;
    }

    private String extractExtension(String originalFileName) {
        try {
            int pos = originalFileName.lastIndexOf(".");
            return pos == -1 ? "jpg" : originalFileName.substring(pos + 1);
        } catch (Exception e) {
            return "jpg";
        }
    }
}
