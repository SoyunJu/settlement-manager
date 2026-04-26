package com.settlement.manager.domain.settlement.repository;

import com.settlement.manager.domain.settlement.entity.Settlement;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByCreatorIdAndYearAndMonth(Long creatorId, int year, int month);

    boolean existsByCreatorIdAndYearAndMonth(Long creatorId, int year, int month);

    // 락 설정
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Settlement s WHERE s.id = :id")
    Optional<Settlement> findByIdWithLock(@Param("id") Long id);

    // 운영자 기간별 집계용
    @Query("SELECT s FROM Settlement s WHERE s.year = :year AND s.month = :month")
    List<Settlement> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    List<Settlement> findByCreatorId(Long creatorId);
}