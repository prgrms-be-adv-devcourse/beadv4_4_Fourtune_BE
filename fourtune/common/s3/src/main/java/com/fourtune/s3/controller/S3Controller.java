package com.fourtune.s3.controller;

import com.fourtune.core.dto.ApiResponse;
import com.fourtune.s3.dto.S3PresignedUrlResponse;
import com.fourtune.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이미지 업로드 (S3)", description = "파일 업로드를 위한 Presigned URL 발급")
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "Presigned URL 발급", description = "S3에 이미지를 업로드할 수 있는 임시 URL을 발급받습니다.")
    @GetMapping("/presigned-url")
    public ApiResponse<S3PresignedUrlResponse> getPresignedUrl(
            @RequestParam String directory,
            @RequestParam String fileName,
            @RequestParam String contentType) {
        S3PresignedUrlResponse response = s3Service.generatePresignedUrl(directory, fileName, contentType);
        return ApiResponse.success(response, "Presigned URL 발급 성공");
    }
}
