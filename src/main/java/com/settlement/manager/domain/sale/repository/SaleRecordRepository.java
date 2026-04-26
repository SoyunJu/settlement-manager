package com.settlement.manager.domain.sale.repository;

import com.settlement.manager.domain.sale.entity.SaleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SaleRecordRepository extends JpaRepository<SaleRecord, Long> {

    // 페이지네이션
    @Query("SELECT s FROM SaleRecord s WHERE s.creatorId = :creatorId AND s.id > :cursor ORDER BY s.id ASC LIMIT :size")
    List<SaleRecord> findByCursor(@Param("creatorId") Long creatorId,
                                  @Param("cursor") Long cursor,
                                  @Param("size") int size);

    // 기간 필터 (UTC 기준)
    @Query("SELECT s FROM SaleRecord s WHERE s.creatorId = :creatorId AND s.paidAt BETWEEN :start AND :end")
    List<SaleRecord> findByCreatorIdAndPaidAtBetween(@Param("creatorId") Long creatorId,
                                                     @Param("start") Instant start,
                                                     @Param("end") Instant end);
}