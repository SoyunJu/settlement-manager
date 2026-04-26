package com.settlement.manager.domain.sale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record CancelRecordCreateRequest(
        @NotNull Long originalSaleId,
        @NotNull @Positive BigDecimal refundAmount,
        @NotNull Instant paidAt
) {}