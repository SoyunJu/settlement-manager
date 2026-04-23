package com.settlement.manager.domain.sale.dto;

import com.settlement.manager.domain.sale.entity.CancelRecord;

import java.math.BigDecimal;
import java.time.Instant;

public record CancelRecordResponse(
        Long id,
        Long creatorId,
        Long originalSaleId,
        BigDecimal refundAmount,
        Instant paidAt
) {
    public static CancelRecordResponse from(CancelRecord record) {
        return new CancelRecordResponse(
                record.getId(),
                record.getCreatorId(),
                record.getOriginalSaleId(),
                record.getRefundAmount(),
                record.getPaidAt()
        );
    }
}