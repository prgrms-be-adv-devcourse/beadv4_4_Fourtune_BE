package com.fourtune.auction.adapter.out.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.common.global.error.ErrorCode;
import com.fourtune.common.global.error.ErrorCodes;
import com.fourtune.common.global.error.ErrorResponse;
import com.fourtune.common.global.error.exception.BusinessException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Feign 4xx/5xx 응답을 BusinessException(ErrorCode)으로 변환.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() < 400) {
            return null;
        }
        ErrorCode errorCode = parseErrorCode(response);
        log.warn("[Feign] {} -> status={}, errorCode={}", methodKey, response.status(), errorCode.getCode());
        return new BusinessException(errorCode);
    }

    private ErrorCode parseErrorCode(Response response) {
        try {
            Response.Body body = response.body();
            if (body == null) {
                return ErrorCodes.defaultForStatus(response.status());
            }
            String bodyStr = new String(body.asInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (bodyStr.isBlank()) {
                return ErrorCodes.defaultForStatus(response.status());
            }
            ErrorResponse errorResponse = objectMapper.readValue(bodyStr, ErrorResponse.class);
            return ErrorCodes.fromCodeOrDefault(
                    errorResponse.getCode(),
                    ErrorCodes.defaultForStatus(response.status())
            );
        } catch (IOException e) {
            log.debug("Feign error body parse failed: {}", e.getMessage());
            return ErrorCodes.defaultForStatus(response.status());
        }
    }
}
