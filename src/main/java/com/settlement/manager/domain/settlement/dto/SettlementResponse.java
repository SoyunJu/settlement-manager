package com.settlement.manager.domain.settlement.dto;

import com.settlement.manager.domain.settlement.entity.Settlement;
import com.settlement.manager.domain.settlement.entity.SettlementStatus;

import java.math.BigDecimal;

public record SettlementResponse(
        Long id,
        Long creatorId,
        int year,
        int month,
        BigDecimal totalSaleAmount,
        BigDecimal totalRefundAmount,
        BigDecimal netSaleAmount,
        BigDecimal feeRate,
        BigDecimal feeAmount,
        BigDecimal settlementAmount,
        int saleCount,
        int cancelCount,
        SettlementStatus status
) {
    public static SettlementResponse from(Settlement s) {
        return new SettlementResponse(
                s.getId(),
                s.getCreatorId(),
                s.getYear(),
                s.getMonth(),
                s.getTotalSaleAmount(),
                s.getTotalRefundAmount(),
                s.getNetSaleAmount(),
                s.getFeeRate(),
                s.getFeeAmount(),
                s.getSettlementAmount(),
                s.getSaleCount(),
                s.getCancelCount(),
                s.getStatus()
        );
    }
}