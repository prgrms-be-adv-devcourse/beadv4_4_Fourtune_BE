package com.fourtune.s3.service;

import com.fourtune.s3.dto.S3PresignedUrlResponse;
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

    @Value("${spring.cloud.aws.s3.path-prefix}")
    private String pathPrefix;

    /**
     * Presigned URL 생성 (업로드용)
     * 
     * @param directory        디렉토리 명
     * @param originalFileName 원본 파일명
     * @param contentType      파일의 MIME 타입 (예: image/jpeg)
     * @return Presigned URL 및 실제 접근 URL
     */
    public S3PresignedUrlResponse generatePresignedUrl(String directory, String originalFileName, String contentType) {
        String fileName = createFileName(directory, originalFileName);

        // Presigned URL 요청 생성 (PUT 메서드용)
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(contentType) // Content-Type 설정 추가
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // 10분간 유효
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        // [수정] 업로드 후 조회 시에는 Presigned GET URL을 사용해야 함 (버킷이 Private일 경우)
        // 하지만 아직 DB에 저장할 땐 일반 URL 포맷으로 저장하고, 조회 시점에 Presigned GET URL을 생성하는 방식이 일반적임.
        // 여기서는 편의상 Key를 반환하거나, 아니면 바로 접근 가능한 URL을 반환해야 하는데
        // 보안상 Private 버킷을 쓴다면 이 URL로는 접근 불가.
        // -> 클라이언트가 이 URL을 '저장' 용도로 쓴다면 OK. 조회할 땐 별도 API 필요.
        String fileKey = fileName;

        log.info("Presigned PUT URL generated: {}", presignedUrl);

        return new S3PresignedUrlResponse(presignedUrl, fileKey);
    }

    /**
     * 조회용 Presigned URL 생성 (GET 메서드용)
     * Private 버킷의 객체를 조회할 때 사용
     * 
     * @param fileKey S3 파일 키 (폴더/파일명)
     * @return 10분간 유효한 조회 URL
     */
    public String generatePresignedGetUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }

        try {
            software.amazon.awssdk.services.s3.model.GetObjectRequest objectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest
                    .builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();

            software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest presignRequest = software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
                    .builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(objectRequest)
                    .build();

            software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest presignedRequest = s3Presigner
                    .presignGetObject(presignRequest);

            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned GET url for key: {}", fileKey, e);
            return null;
        }
    }

    private String createFileName(String directory, String originalFileName) {
        String ext = extractExtension(originalFileName);
        // [수정] directory가 없으면 바로 pathPrefix 아래에 저장
        if (directory == null || directory.isBlank()) {
            return pathPrefix + "/" + UUID.randomUUID().toString() + "." + ext;
        }
        return pathPrefix + "/" + directory + "/" + UUID.randomUUID().toString() + "." + ext;
    }

    private String extractExtension(String originalFileName) {
        try {
            int pos = originalFileName.lastIndexOf(".");
            if (pos == -1) {
                return "jpg";
            }
            return originalFileName.substring(pos + 1);
        } catch (Exception e) {
            return "jpg"; // 예외 발생 시 기본값은 jpg로 지정함
        }
    }
}
