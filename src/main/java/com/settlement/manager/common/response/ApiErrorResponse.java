package com.settlement.manager.common.response;

import lombok.Getter;

// HTTP 상태코드 노출 금지. code + message만 반환.
@Getter
public class ApiErrorResponse {

    private final String code;
    private final String message;

    public ApiErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}