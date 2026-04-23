package com.settlement.manager.domain.feerate.entity;

import com.settlement.manager.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "fee_rate")
public class FeeRate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기본 수수료율
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    // 변경 userId
    @Column(nullable = false)
    private Long changedBy;

    // 등급별 수수료 override
    @Enumerated(EnumType.STRING)
    private CreatorGrade grade;

    @Builder
    public FeeRate(BigDecimal rate, Long changedBy, CreatorGrade grade) {
        this.rate = rate;
        this.changedBy = changedBy;
        this.grade = grade;
    }
}