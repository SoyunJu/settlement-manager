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
@Table(name = "sale_record",
        indexes = {
                @Index(name = "idx_sale_creator_paid_at", columnList = "creator_id, paid_at"),
                @Index(name = "idx_sale_cursor", columnList = "id")
        })
public class SaleRecord extends BaseEntity {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // UTC 저장. 기준값.
    @Column(name = "paid_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant paidAt;

    // KST 변환값 저장.
    @Column(name = "paid_at_kst", columnDefinition = "TIMESTAMP")
    private LocalDateTime paidAtKst;

    @Builder
    public SaleRecord(Long creatorId, Long courseId, Long studentId,
                      BigDecimal amount, Instant paidAt) {
        this.creatorId = creatorId;
        this.courseId = courseId;
        this.studentId = studentId;
        setPaidAt(paidAt);
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
        this.paidAtKst = paidAt.atZone(KST).toLocalDateTime();
    }
}