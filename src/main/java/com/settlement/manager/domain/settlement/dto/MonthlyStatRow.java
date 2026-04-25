package com.settlement.manager.domain.settlement.dto;

import java.math.BigDecimal;

public record MonthlyStatRow(
        int year,
        int month,
        BigDecimal totalSaleAmount,
        BigDecimal totalRefundAmount,
        BigDecimal netSaleAmount,
        BigDecimal feeAmount,
        BigDecimal settlementAmount,
        long settlementCount
) {}