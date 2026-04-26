package com.settlement.manager.api.v1;

import com.settlement.manager.common.response.ApiResponse;
import com.settlement.manager.domain.sale.dto.CreatorStatsResponse;
import com.settlement.manager.domain.sale.service.CreatorStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CreatorStats", description = "크리에이터 수익 통계 API")
@RestController
@RequestMapping("/api/v1/creator-stats")
@RequiredArgsConstructor
public class CreatorStatsController {

    private final CreatorStatsService creatorStatsService;

    @Operation(summary = "강의별 월별 수익 추이")
    @GetMapping("/{creatorId}/courses")
    public ResponseEntity<ApiResponse<CreatorStatsResponse>> getCourseStats(
            @PathVariable Long creatorId,
            @AuthenticationPrincipal Long currentUserId) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OPERATOR"));
        return ResponseEntity.ok(ApiResponse.of(
                creatorStatsService.getCourseStats(creatorId, currentUserId, isAdmin)));
    }
}