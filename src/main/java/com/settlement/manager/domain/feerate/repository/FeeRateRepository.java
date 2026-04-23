package com.settlement.manager.domain.feerate.repository;

import com.settlement.manager.domain.feerate.entity.CreatorGrade;
import com.settlement.manager.domain.feerate.entity.FeeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface FeeRateRepository extends JpaRepository<FeeRate, Long> {

    // 최근 기본 수수료율 조회 (등급 null)
    Optional<FeeRate> findTopByGradeIsNullAndCreatedAtBeforeOrderByCreatedAtDesc(Instant at);

    // 최근 등급별 수수료율 조회
    Optional<FeeRate> findTopByGradeAndCreatedAtBeforeOrderByCreatedAtDesc(CreatorGrade grade, Instant at);

    // 현재 기본 수수료율 조회
    Optional<FeeRate> findTopByGradeIsNullOrderByCreatedAtDesc();

    // 현재 등급별 수수료율 조회
    Optional<FeeRate> findTopByGradeOrderByCreatedAtDesc(CreatorGrade grade);
}