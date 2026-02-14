package com.fourtune.common.global.error;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ErrorCode 역매핑 유틸리티.
 * Feign ErrorDecoder 등에서 응답 body의 code 문자열(예: "O001")을 ErrorCode enum으로 변환할 때 사용.
 */
public final class ErrorCodes {

    private static final Map<String, ErrorCode> CODE_MAP = new ConcurrentHashMap<>();

    static {
        for (ErrorCode ec : ErrorCode.values()) {
            CODE_MAP.put(ec.getCode(), ec);
        }
    }

    private ErrorCodes() {
    }

    /**
     * code 문자열에 해당하는 ErrorCode 반환.
     * @param code 예: "O001", "U001"
     * @return 해당 ErrorCode, 없으면 empty
     */
    public static Optional<ErrorCode> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(CODE_MAP.get(code.trim()));
    }

    /**
     * code 문자열에 해당하는 ErrorCode 반환. 없으면 기본값 반환.
     */
    public static ErrorCode fromCodeOrDefault(String code, ErrorCode defaultCode) {
        return fromCode(code).orElse(defaultCode);
    }

    /**
     * HTTP status에 따른 기본 ErrorCode (body 파싱 실패 시 사용).
     * 서비스별로 다른 기본값이 필요하면 FeignErrorDecoder에서 fromCodeOrDefault(..., serviceDefault) 사용.
     */
    public static ErrorCode defaultForStatus(int status) {
        if (status == 404) {
            return ErrorCode.USER_NOT_FOUND;
        }
        if (status >= 500) {
            return ErrorCode.INTERNAL_SERVER_ERROR;
        }
        return ErrorCode.INVALID_INPUT_VALUE;
    }
}
