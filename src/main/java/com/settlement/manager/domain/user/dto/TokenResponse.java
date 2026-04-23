package com.settlement.manager.domain.user.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}