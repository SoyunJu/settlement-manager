package com.settlement.manager.domain.sale.dto;

import java.util.List;
import java.util.Map;

public record CreatorStatsResponse(
        Long creatorId,
        Map<Long, List<CourseMonthlyStatRow>> courseStats // 월별 수익 list
) {}