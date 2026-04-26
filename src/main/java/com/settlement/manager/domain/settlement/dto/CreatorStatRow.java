package com.settlement.manager.domain.settlement.dto;

import com.settlement.manager.domain.settlement.entity.SettlementStatus;

import java.math.BigDecimal;

public record CreatorStatRow(
        Long creatorId,
        BigDecimal totalSaleAmount,
        BigDecimal netSaleAmount,
        BigDecimal settlementAmount,
        SettlementStatus status
) {}