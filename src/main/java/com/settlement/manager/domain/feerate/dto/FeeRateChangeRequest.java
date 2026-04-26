package com.settlement.manager.domain.feerate.dto;

import com.settlement.manager.domain.feerate.entity.CreatorGrade;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FeeRateChangeRequest(
        @NotNull
        @DecimalMin("0.0000")
        @DecimalMax("1.0000")
        BigDecimal rate,

        // override
        CreatorGrade grade
) {}