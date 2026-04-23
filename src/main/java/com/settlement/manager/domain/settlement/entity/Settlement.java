package com.settlement.manager.domain.settlement.entity;

import com.settlement.manager.common.entity.BaseEntity;
import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_settlement_creator_period",
                columnNames = {"creator_id", "year", "month"}
        ),
        indexes = @Index(name = "idx_settlement_creator_period", columnList = "creator_id, year, month")
)
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    // 총 판매금액
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalSaleAmount;

    // 총 환불금액
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalRefundAmount;

    // 순이익 = 총판매 - 총환불
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal netSaleAmount;

    // 수수료율 (정산 시점 기준)
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal feeRate;

    // 수수료 금액 = 순이익 * feeRate
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal feeAmount;

    // 정산 예정 금액 = 순이익 - 수수료
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal settlementAmount;

    @Column(nullable = false)
    private int saleCount;

    @Column(nullable = false)
    private int cancelCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @Builder
    public Settlement(Long creatorId, int year, int month,
                      BigDecimal totalSaleAmount, BigDecimal totalRefundAmount,
                      BigDecimal netSaleAmount, BigDecimal feeRate,
                      BigDecimal feeAmount, BigDecimal settlementAmount,
                      int saleCount, int cancelCount) {
        this.creatorId = creatorId;
        this.year = year;
        this.month = month;
        this.totalSaleAmount = totalSaleAmount;
        this.totalRefundAmount = totalRefundAmount;
        this.netSaleAmount = netSaleAmount;
        this.feeRate = feeRate;
        this.feeAmount = feeAmount;
        this.settlementAmount = settlementAmount;
        this.saleCount = saleCount;
        this.cancelCount = cancelCount;
        this.status = SettlementStatus.PENDING;
    }

    // 역행 불가.
    public void confirm() {
        if (this.status != SettlementStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = SettlementStatus.CONFIRMED;
    }
    // 역행 불가.
    public void pay() {
        if (this.status != SettlementStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = SettlementStatus.PAID;
    }
}