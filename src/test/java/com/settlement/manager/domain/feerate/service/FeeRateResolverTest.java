package com.settlement.manager.domain.feerate.service;

import com.settlement.manager.domain.feerate.entity.CreatorGrade;
import com.settlement.manager.domain.feerate.entity.FeeRate;
import com.settlement.manager.domain.feerate.repository.FeeRateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FeeRateResolverTest {

    @InjectMocks
    private FeeRateResolver feeRateResolver;

    @Mock
    private FeeRateRepository feeRateRepository;

    @Test
    @DisplayName("등급별 수수료 override -> GOLD 15%")
    void grade_override_applied() {
        FeeRate goldRate = FeeRate.builder()
                .rate(new BigDecimal("0.15"))
                .grade(CreatorGrade.GOLD)
                .changedBy(1L)
                .build();
        given(feeRateRepository.findTopByGradeAndCreatedAtBeforeOrderByCreatedAtDesc(
                eq(CreatorGrade.GOLD), any(Instant.class)))
                .willReturn(Optional.of(goldRate));

        BigDecimal result = feeRateResolver.resolve(CreatorGrade.GOLD, Instant.now());

        assertThat(result).isEqualByComparingTo("0.15");
    }

    @Test
    @DisplayName("등급 override 없으면 -> 기본 수수료율 적용")
    void fallback_to_base_rate() {
        FeeRate baseRate = FeeRate.builder()
                .rate(new BigDecimal("0.20"))
                .grade(null)
                .changedBy(1L)
                .build();
        given(feeRateRepository.findTopByGradeAndCreatedAtBeforeOrderByCreatedAtDesc(
                eq(CreatorGrade.BRONZE), any(Instant.class)))
                .willReturn(Optional.empty());
        given(feeRateRepository.findTopByGradeIsNullAndCreatedAtBeforeOrderByCreatedAtDesc(any()))
                .willReturn(Optional.of(baseRate));

        BigDecimal result = feeRateResolver.resolve(CreatorGrade.BRONZE, Instant.now());

        assertThat(result).isEqualByComparingTo("0.20");
    }

    @Test
    @DisplayName("DB에 히스토리 없으면 -> 기본 수수료율")
    void fallback_to_env_default() {
        ReflectionTestUtils.setField(feeRateResolver, "defaultFeeRate", new BigDecimal("0.20"));

        given(feeRateRepository.findTopByGradeAndCreatedAtBeforeOrderByCreatedAtDesc(any(), any()))
                .willReturn(Optional.empty());
        given(feeRateRepository.findTopByGradeIsNullAndCreatedAtBeforeOrderByCreatedAtDesc(any()))
                .willReturn(Optional.empty());

        BigDecimal result = feeRateResolver.resolve(CreatorGrade.BRONZE, Instant.now());

        assertThat(result).isEqualByComparingTo("0.20");
    }

    @Test
    @DisplayName("grade null -> 기본 수수료율")
    void null_grade_uses_base_rate() {
        FeeRate baseRate = FeeRate.builder()
                .rate(new BigDecimal("0.20"))
                .grade(null)
                .changedBy(1L)
                .build();
        given(feeRateRepository.findTopByGradeIsNullAndCreatedAtBeforeOrderByCreatedAtDesc(any()))
                .willReturn(Optional.of(baseRate));

        BigDecimal result = feeRateResolver.resolve(null, Instant.now());

        assertThat(result).isEqualByComparingTo("0.20");
    }
}