package com.fourtune.auction.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common(공통 에러)
    INVALID_INPUT_VALUE(400, "C001", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(405, "C002", "허용되지 않은 메서드입니다."),
    INTERNAL_SERVER_ERROR(500, "C003", "서버 내부 오류가 발생했습니다."),

    //User(유저 관련)
    USER_NOT_FOUND(404, "U001", "존재하지 않는 사용자입니다."),
    EMAIL_DUPLICATION(400, "U002", "이미 가입된 이메일입니다."),
    LOGIN_INPUT_INVALID(400, "U003", "로그인 정보가 일치하지 않습니다.");

    private final int status;
    private final String code;
    private final String message;

}
