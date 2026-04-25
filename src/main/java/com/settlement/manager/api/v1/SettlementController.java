package com.settlement.manager.api.v1;

import com.settlement.manager.common.response.ApiResponse;
import com.settlement.manager.domain.settlement.dto.SettlementResponse;
import com.settlement.manager.domain.settlement.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Settlement", description = "정산 API")
@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @Operation(summary = "월별 정산 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<SettlementResponse>> getOrCreate(
            @RequestParam Long creatorId,
            @RequestParam String yearMonth,
            @AuthenticationPrincipal Long currentUserId) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.of(
                settlementService.getOrCreate(creatorId, yearMonth, currentUserId, isAdmin)));
    }

    @Operation(summary = "정산 확정 (OPERATOR 이상)")
    @PostMapping("/{settlementId}/confirm")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<SettlementResponse>> confirm(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(ApiResponse.of(settlementService.confirm(settlementId, currentUserId)));
    }

    @Operation(summary = "정산 지급 완료 (OPERATOR 이상)")
    @PostMapping("/{settlementId}/pay")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<SettlementResponse>> pay(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(ApiResponse.of(settlementService.pay(settlementId, currentUserId)));
    }
}