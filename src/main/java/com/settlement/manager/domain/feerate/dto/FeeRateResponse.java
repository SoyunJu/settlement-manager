package com.settlement.manager.domain.feerate.dto;

import com.settlement.manager.domain.feerate.entity.CreatorGrade;
import com.settlement.manager.domain.feerate.entity.FeeRate;

import java.math.BigDecimal;
import java.time.Instant;

public record FeeRateResponse(
        Long id,
        BigDecimal rate,
        CreatorGrade grade,
        Long changedBy,
        Instant createdAt
) {
    public static FeeRateResponse from(FeeRate feeRate) {
        return new FeeRateResponse(
                feeRate.getId(),
                feeRate.getRate(),
                feeRate.getGrade(),
                feeRate.getChangedBy(),
                feeRate.getCreatedAt()
        );
    }
}