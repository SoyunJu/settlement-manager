package com.settlement.manager.domain.settlement.repository;

import com.settlement.manager.domain.settlement.dto.MonthlyStatRow;
import com.settlement.manager.domain.settlement.dto.CreatorStatRow;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardQueryRepository {

    private final EntityManager em;

    // 전체 월별 집계
    @SuppressWarnings("unchecked")
    public List<MonthlyStatRow> findMonthlyStats(int year, int month) {
        String jpql = """
                SELECT new com.settlement.manager.domain.settlement.dto.MonthlyStatRow(
                    s.year, s.month,
                    SUM(s.totalSaleAmount),
                    SUM(s.totalRefundAmount),
                    SUM(s.netSaleAmount),
                    SUM(s.feeAmount),
                    SUM(s.settlementAmount),
                    COUNT(s)
                )
                FROM Settlement s
                WHERE s.year = :year AND s.month = :month
                GROUP BY s.year, s.month
                """;
        return em.createQuery(jpql)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

    // 크리에이터별 집계
    @SuppressWarnings("unchecked")
    public List<CreatorStatRow> findCreatorStats(int year, int month) {
        String jpql = """
                SELECT new com.settlement.manager.domain.settlement.dto.CreatorStatRow(
                    s.creatorId,
                    SUM(s.totalSaleAmount),
                    SUM(s.netSaleAmount),
                    SUM(s.settlementAmount),
                    s.status
                )
                FROM Settlement s
                WHERE s.year = :year AND s.month = :month
                GROUP BY s.creatorId, s.status
                ORDER BY SUM(s.settlementAmount) DESC
                """;
        return em.createQuery(jpql)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }
}