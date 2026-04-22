package com.settlement.manager.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    UNAUTHORIZED(401,  "AUTH_001", "인증이 필요합니다"),
    FORBIDDEN(403,     "AUTH_002", "접근 권한이 없습니다"),
    INVALID_TOKEN(401, "AUTH_003", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(401, "AUTH_004", "토큰이 만료되었습니다"),
    INVALID_INPUT(400, "COMMON_001", "입력값이 올바르지 않습니다"),
    TOO_MANY_REQUESTS(429, "COMMON_002", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요"),
    INTERNAL_ERROR(500, "COMMON_003", "처리 중 오류가 발생했습니다"),

    // 회원
    USER_NOT_FOUND(404,         "USER_001", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(409,        "USER_002", "이미 사용 중인 이메일입니다"),
    ACCOUNT_LOCKED(403,         "USER_003", "계정이 잠겼습니다. 운영자에게 문의하세요"),
    ACCOUNT_ALREADY_LOCKED(400, "USER_004", "이미 잠긴 계정입니다"),
    USER_ALREADY_DELETED(400,   "USER_005", "이미 탈퇴한 계정입니다"),

    // 정산
    SETTLEMENT_ALREADY_EXISTS(409, "SETTLE_001", "해당 기간 정산이 이미 존재합니다"),
    SETTLEMENT_NOT_FOUND(404,      "SETTLE_002", "정산 내역을 찾을 수 없습니다"),
    INVALID_STATUS_TRANSITION(400, "SETTLE_003", "유효하지 않은 상태 전이입니다"),
    INVALID_PERIOD_FORMAT(400,     "SETTLE_004", "연월 형식이 올바르지 않습니다 (YYYY-MM)"),
    FUTURE_PERIOD_NOT_ALLOWED(400, "SETTLE_005", "미래 날짜는 조회할 수 없습니다"),

    // 판매
    REFUND_EXCEEDS_ORIGINAL(400, "SALE_001", "환불 금액이 원결제 금액을 초과합니다"),
    SALE_NOT_FOUND(404,          "SALE_002", "판매 내역을 찾을 수 없습니다"),

    // 수수료
    FEE_RATE_NOT_FOUND(404, "FEE_001", "수수료율을 찾을 수 없습니다"),
    ;

    // httpStatus는 내부에서만 사용. body에는 code + message만 노출.
    private final int httpStatus;
    private final String code;
    private final String message;
}