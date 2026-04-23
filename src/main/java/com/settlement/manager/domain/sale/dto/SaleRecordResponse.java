package com.settlement.manager.domain.sale.dto;

import com.settlement.manager.domain.sale.entity.SaleRecord;

import java.math.BigDecimal;
import java.time.Instant;

public record SaleRecordResponse(
        Long id,
        Long creatorId,
        Long courseId,
        Long studentId,
        BigDecimal amount,
        Instant paidAt
) {
    public static SaleRecordResponse from(SaleRecord record) {
        return new SaleRecordResponse(
                record.getId(),
                record.getCreatorId(),
                record.getCourseId(),
                record.getStudentId(),
                record.getAmount(),
                record.getPaidAt()
        );
    }
}