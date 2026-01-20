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
    FORBIDDEN(403, "C004", "권한이 없습니다."),

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
    AUCTION_INVALID_DURATION(400, "A006", "유효하지 않은 경매 기간입니다."),
    AUCTION_HAS_BIDS(400, "A007", "입찰이 있는 경매는 삭제할 수 없습니다."),
    AUCTION_IN_PROGRESS(400, "A008", "진행중인 경매는 삭제할 수 없습니다."),
    
    //Bid(입찰 관련)
    BID_NOT_FOUND(404, "B001", "존재하지 않는 입찰입니다."),
    BID_AMOUNT_TOO_LOW(400, "B002", "입찰가가 현재가보다 낮습니다."),
    BID_UNIT_INVALID(400, "B003", "입찰 단위가 맞지 않습니다."),
    BID_NOT_ALLOWED(400, "B004", "입찰할 수 없는 경매 상태입니다."),
    BID_SELF_AUCTION(400, "B005", "자신의 경매에는 입찰할 수 없습니다."),
    BID_ALREADY_HIGHEST(400, "B006", "이미 최고 입찰자입니다."),
    BID_CANCELLED_NOT_ALLOWED(400, "B007", "취소할 수 없는 입찰입니다."),
    BID_LOCK_FAILED(500, "B008", "입찰 처리 중 오류가 발생했습니다."),
    
    //Order(주문 관련)
    ORDER_NOT_FOUND(404, "O001", "존재하지 않는 주문입니다."),
    ORDER_ALREADY_EXISTS(409, "O002", "이미 생성된 주문이 있습니다."),
    ORDER_ALREADY_COMPLETED(400, "O003", "이미 완료된 주문입니다."),
    ORDER_AMOUNT_MISMATCH(400, "O004", "주문 금액이 일치하지 않습니다."),
    ORDER_ALREADY_PROCESSED(400, "O005", "이미 처리된 주문입니다."),
    ORDER_CANCEL_NOT_ALLOWED(400, "O006", "취소할 수 없는 주문입니다."),
    ORDER_ALREADY_CANCELLED(400, "O007", "이미 취소된 주문입니다."),
    ORDER_INVALID_AMOUNT(400, "O008", "유효하지 않은 주문 금액입니다."),
    
    //Cart(장바구니 관련)
    CART_NOT_FOUND(404, "CA001", "존재하지 않는 장바구니입니다."),
    CART_ITEM_NOT_FOUND(404, "CA002", "장바구니에 존재하지 않는 상품입니다."),
    CART_ITEM_ALREADY_EXISTS(409, "CA003", "이미 장바구니에 담긴 상품입니다."),
    CART_ITEM_NOT_ACTIVE(400, "CA004", "활성 상태가 아닌 장바구니 아이템입니다."),
    CART_ITEM_EXPIRED(400, "CA005", "만료된 장바구니 아이템입니다."),
    
    //BuyNow(즉시구매 관련)
    BUY_NOW_NOT_ENABLED(400, "BN001", "즉시구매가 활성화되지 않은 경매입니다."),
    BUY_NOW_PRICE_NOT_SET(400, "BN002", "즉시구매가가 설정되지 않았습니다."),
    AUCTION_NOT_ACTIVE(400, "BN003", "진행 중인 경매가 아닙니다."),
    CANNOT_ADD_TO_CART(400, "BN004", "장바구니에 담을 수 없는 경매 상품입니다."),
    CANNOT_BUY_OWN_ITEM(400, "BN005", "자신의 상품은 구매할 수 없습니다."),
    
    //JwtToken(토큰 관련)
    EXPIRED_ACCESS_TOKEN(401, "T001", "ACCESS 토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(401, "TOO2", "REFRESH 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(401, "T003", "유효하지 않은 REFRESH 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(401, "T004", "리프레시 토큰이 일치하지 않습니다.(해킹 의심)");

    private final int status;
    private final String code;
    private final String message;

}
