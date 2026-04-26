package com.settlement.manager.domain.sale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record SaleRecordCreateRequest(
        @NotNull Long creatorId,
        @NotNull Long courseId,
        @NotNull Long studentId,
        @NotNull @Positive BigDecimal amount,
        @NotNull Instant paidAt
) {}