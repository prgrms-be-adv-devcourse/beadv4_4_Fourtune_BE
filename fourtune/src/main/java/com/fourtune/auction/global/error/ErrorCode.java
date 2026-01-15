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
    EMAIL_DUPLICATION(409, "U002", "이미 가입된 이메일입니다."),
    NICKNAME_DUPLICATION(409, "U003", "이미 가입된 닉네임입니다."),
    PHONE_DUPLICATION(409, "U004", "이미 가입된 전화번호입니다."),
    LOGIN_INPUT_INVALID(401, "U005", "로그인 정보가 일치하지 않습니다."),
    PASSWORD_NOT_MATCH(400, "U006", "비밀번호가 일치하지 않습니다."),
    PASSWORD_SAME_AS_OLD(400, "U007", "비밀번호가 이전과 동일합니다."),
    ALREADY_WITHDRAWN(400, "U008", "이미 탈퇴한 사용자입니다."),

    //Lock(락 관련)
    CHANGE_CONFLICT(409, "U008", "다른 사용자에 의해 정보가 변경되었습니다. 다시 시도해주세요"),

    //Auction(경매 관련)
    AUCTION_NOT_FOUND(404, "A001", "존재하지 않는 경매입니다."),
    AUCTION_NOT_MODIFIABLE(400, "A002", "수정할 수 없는 경매 상태입니다."),
    AUCTION_SELLER_MISMATCH(403, "A003", "판매자만 수정할 수 있습니다."),
    AUCTION_ALREADY_ENDED(400, "A004", "이미 종료된 경매입니다."),
    AUCTION_INVALID_PRICE(400, "A005", "유효하지 않은 가격입니다."),
    AUCTION_INVALID_DURATION(400, "A006", "유효하지 않은 경매 기간입니다.");
    //JwtToken(토큰 관련)
    EXPIRED_ACCESS_TOKEN(401, "T001", "ACCESS 토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(401, "TOO2", "REFRESH 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(401, "T003", "유효하지 않은 REFRESH 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(401, "T004", "리프레시 토큰이 일치하지 않습니다.(해킹 의심)");

    private final int status;
    private final String code;
    private final String message;

}
