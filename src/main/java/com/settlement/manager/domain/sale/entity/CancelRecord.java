package com.settlement.manager.domain.sale.entity;

import com.settlement.manager.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "cancel_record",
        indexes = {
                @Index(name = "idx_cancel_creator_paid_at", columnList = "creator_id, paid_at"),
                @Index(name = "idx_cancel_original_sale", columnList = "original_sale_id")
        })
public class CancelRecord extends BaseEntity {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    // 결제 레코드 참조용
    @Column(name = "original_sale_id", nullable = false)
    private Long originalSaleId;

    // 환불액
    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    // UTC 저장. 계산 기준.
    @Column(name = "paid_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant paidAt;

    // KST 변환값 저장. 편의용.
    @Column(name = "paid_at_kst", columnDefinition = "TIMESTAMP")
    private LocalDateTime paidAtKst;

    @Builder
    public CancelRecord(Long creatorId, Long originalSaleId,
                        BigDecimal refundAmount, Instant paidAt) {
        this.creatorId = creatorId;
        this.originalSaleId = originalSaleId;
        setPaidAt(paidAt);
        this.refundAmount = refundAmount;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
        this.paidAtKst = paidAt.atZone(KST).toLocalDateTime();
    }
}