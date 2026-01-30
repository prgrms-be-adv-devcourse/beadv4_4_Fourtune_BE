package com.fourtune.auction.infra.s3.controller;

import com.fourtune.auction.infra.s3.dto.S3PresignedUrlResponse;
import com.fourtune.auction.infra.s3.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class S3ControllerTest {

    private MockMvc mockMvc;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private S3Controller s3Controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(s3Controller).build();
    }

    @Test
    @DisplayName("Presigned URL 발급 API 성공 테스트")
    void getPresignedUrl_success() throws Exception {
        // given
        String directory = "auction";
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        String presignedUrl = "https://s3.aws.com/presigned";
        String imageUrl = "https://s3.aws.com/image.jpg";

        given(s3Service.generatePresignedUrl(directory, fileName, contentType))
                .willReturn(new S3PresignedUrlResponse(presignedUrl, imageUrl));

        // when & then
        mockMvc.perform(get("/api/v1/images/presigned-url")
                .param("directory", directory)
                .param("fileName", fileName)
                .param("contentType", contentType)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Presigned URL 발급 성공"))
                .andExpect(jsonPath("$.data.presignedUrl").value(presignedUrl))
                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl));
    }
}
