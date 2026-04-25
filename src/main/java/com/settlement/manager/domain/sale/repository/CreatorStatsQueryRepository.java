package com.settlement.manager.domain.sale.repository;

import com.settlement.manager.domain.sale.dto.CourseMonthlyStatRow;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CreatorStatsQueryRepository {

    private final EntityManager em;

    // 강의별 월별 수익 통계 (KST 변환 집계)
    @SuppressWarnings("unchecked")
    public List<CourseMonthlyStatRow> findCourseMonthlyStats(Long creatorId) {
        String jpql = """
                SELECT new com.settlement.manager.domain.sale.dto.CourseMonthlyStatRow(
                    s.courseId,
                    FUNCTION('date_trunc', 'month',
                        FUNCTION('timezone', 'Asia/Seoul', s.paidAt)),
                    SUM(s.amount),
                    COUNT(s)
                )
                FROM SaleRecord s
                WHERE s.creatorId = :creatorId
                GROUP BY s.courseId,
                    FUNCTION('date_trunc', 'month',
                        FUNCTION('timezone', 'Asia/Seoul', s.paidAt))
                ORDER BY s.courseId ASC,
                    FUNCTION('date_trunc', 'month',
                        FUNCTION('timezone', 'Asia/Seoul', s.paidAt)) DESC
                """;
        return em.createQuery(jpql)
                .setParameter("creatorId", creatorId)
                .getResultList();
    }
}