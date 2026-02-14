package com.fourtune.common.global.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /** Feign 등에서 JSON 역직렬화용 (서비스 간 에러 body 파싱). @JsonCreator로 역직렬화 안정화. */
    @JsonCreator
    public ErrorResponse(
            @JsonProperty("status") int status,
            @JsonProperty("code") String code,
            @JsonProperty("message") String message) {
        this.status = status;
        this.code = code != null ? code : "";
        this.message = message != null ? message : "";
    }

    public static ErrorResponse of(ErrorCode errorCode){
        return new ErrorResponse(errorCode);
    }

}
