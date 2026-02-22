package com.fourtune.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 발급 응답")
public record S3PresignedUrlResponse(
                @Schema(description = "S3에 업로드(PUT)할 수 있는 Presigned URL") String presignedUrl,

                @Schema(description = "업로드 후 접근 가능한 실제 이미지 URL (CDN/S3)") String imageUrl) {
}
