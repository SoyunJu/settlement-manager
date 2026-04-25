package com.settlement.manager.domain.settlement.dto;

import java.util.List;

public record DashboardResponse(
        MonthlyStatRow summary,
        List<CreatorStatRow> creators
) {}