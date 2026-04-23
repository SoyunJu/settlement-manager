package com.settlement.manager.domain.sale.repository;

import com.settlement.manager.domain.sale.entity.CancelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CancelRecordRepository extends JpaRepository<CancelRecord, Long> {

    // 필터 조회 (UTC 기준)
    @Query("SELECT c FROM CancelRecord c WHERE c.creatorId = :creatorId AND c.paidAt BETWEEN :start AND :end")
    List<CancelRecord> findByCreatorIdAndPaidAtBetween(@Param("creatorId") Long creatorId,
                                                       @Param("start") Instant start,
                                                       @Param("end") Instant end);
}