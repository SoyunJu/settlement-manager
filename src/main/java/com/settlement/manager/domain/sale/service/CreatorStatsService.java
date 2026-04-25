package com.settlement.manager.domain.sale.service;

import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import com.settlement.manager.domain.sale.dto.CourseMonthlyStatRow;
import com.settlement.manager.domain.sale.dto.CreatorStatsResponse;
import com.settlement.manager.domain.sale.repository.CreatorStatsQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatorStatsService {

    private final CreatorStatsQueryRepository creatorStatsQueryRepository;

    // 강의별 통계 조회
    @Transactional(readOnly = true)
    public CreatorStatsResponse getCourseStats(Long creatorId, Long currentUserId, boolean isAdmin) {
        if (!isAdmin && !creatorId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<CourseMonthlyStatRow> rows =
                creatorStatsQueryRepository.findCourseMonthlyStats(creatorId);

        // courseId 그룹핑
        Map<Long, List<CourseMonthlyStatRow>> courseStats = rows.stream()
                .collect(Collectors.groupingBy(CourseMonthlyStatRow::courseId));

        log.debug("강의별 수익 조회. creatorId={} courseCount={}", creatorId, courseStats.size());
        return new CreatorStatsResponse(creatorId, courseStats);
    }
}