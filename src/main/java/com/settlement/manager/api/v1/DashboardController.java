package com.settlement.manager.api.v1;

import com.settlement.manager.common.response.ApiResponse;
import com.settlement.manager.domain.settlement.dto.DashboardResponse;
import com.settlement.manager.domain.settlement.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dashboard", description = "운영자 대시보드 API")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "월별 대시보드 조회")
    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam String yearMonth) {
        return ResponseEntity.ok(ApiResponse.of(dashboardService.getDashboard(yearMonth)));
    }

    @Operation(summary = "정산 강제 갱신 (ADMIN 전용)")
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> refresh(@RequestParam String yearMonth) {
        dashboardService.refreshCache(yearMonth);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}