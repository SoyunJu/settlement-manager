package com.settlement.manager.api.v1;

import com.settlement.manager.common.response.ApiResponse;
import com.settlement.manager.domain.feerate.dto.FeeRateChangeRequest;
import com.settlement.manager.domain.feerate.dto.FeeRateResponse;
import com.settlement.manager.domain.feerate.service.FeeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FeeRate", description = "수수료율 API")
@RestController
@RequestMapping("/api/v1/fee-rates")
@RequiredArgsConstructor
public class FeeRateController {

    private final FeeRateService feeRateService;

    @Operation(summary = "현재 수수료율 조회")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<FeeRateResponse>> getCurrent() {
        return ResponseEntity.ok(ApiResponse.of(feeRateService.getCurrent()));
    }

    @Operation(summary = "수수료율 변경 (ADMIN 전용)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeRateResponse>> change(
            @Valid @RequestBody FeeRateChangeRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(ApiResponse.of(feeRateService.change(request, currentUserId)));
    }

    @Operation(summary = "수수료율 History 조회 (ADMIN 전용)")
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeeRateResponse>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.of(feeRateService.getHistory()));
    }
}