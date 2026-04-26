package com.settlement.manager.domain.feerate.service;

import com.settlement.manager.domain.feerate.entity.CreatorGrade;
import com.settlement.manager.domain.feerate.repository.FeeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeeRateResolver {

    private final FeeRateRepository feeRateRepository;

    @Value("${app.fee-rate.default}")
    private BigDecimal defaultFeeRate;

    // 정산 시점 기준 수수료율 조회
    public BigDecimal resolve(CreatorGrade grade, Instant at) {
        // 등급별 override
        if (grade != null) {
            var gradeRate = feeRateRepository
                    .findTopByGradeAndCreatedAtBeforeOrderByCreatedAtDesc(grade, at);
            if (gradeRate.isPresent()) {
                log.debug("등급별 수수료 적용. grade={} rate={}", grade, gradeRate.get().getRate());
                return gradeRate.get().getRate();
            }
        }

        // 기본 수수료율 조회
        var baseRate = feeRateRepository
                .findTopByGradeIsNullAndCreatedAtBeforeOrderByCreatedAtDesc(at);
        if (baseRate.isPresent()) {
            log.debug("기본 수수료 적용. rate={}", baseRate.get().getRate());
            return baseRate.get().getRate();
        }

        log.warn("기본 수수료율 적용 : rate={}", defaultFeeRate);
        return defaultFeeRate;
    }


    // 현재 수수료율 조회
    public BigDecimal resolveCurrent(CreatorGrade grade) {
        if (grade != null) {
            var gradeRate = feeRateRepository.findTopByGradeOrderByCreatedAtDesc(grade);
            if (gradeRate.isPresent()) {
                return gradeRate.get().getRate();
            }
        }
        return feeRateRepository.findTopByGradeIsNullOrderByCreatedAtDesc()
                .map(FeeRate -> FeeRate.getRate())
                .orElse(defaultFeeRate);
    }
}