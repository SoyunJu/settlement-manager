package com.settlement.manager.domain.sale.dto;

import java.util.List;

public record SaleRecordPageResponse(
        List<SaleRecordResponse> content,
        Long nextCursor,
        boolean hasNext
) {}