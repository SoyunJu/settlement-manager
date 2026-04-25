package com.settlement.manager.domain.sale.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CourseMonthlyStatRow(
        Long courseId,
        Instant monthKst,   // KST 변환 date
        BigDecimal totalAmount,
        long saleCount
) {}