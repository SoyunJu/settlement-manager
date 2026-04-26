package com.settlement.manager.api.v1;

import com.settlement.manager.common.response.ApiResponse;
import com.settlement.manager.domain.sale.dto.*;
import com.settlement.manager.domain.sale.service.SaleRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "SaleRecord", description = "판매 내역 API")
@RestController
@RequestMapping("/api/v1/sale-records")
@RequiredArgsConstructor
public class SaleRecordController {

    private final SaleRecordService saleRecordService;

    @Operation(summary = "판매 내역 등록 (ADMIN 전용)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SaleRecordResponse>> create(
            @Valid @RequestBody SaleRecordCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.of(saleRecordService.create(request)));
    }

    @Operation(summary = "취소 내역 등록")
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<CancelRecordResponse>> createCancel(
            @Valid @RequestBody CancelRecordCreateRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(ApiResponse.of(saleRecordService.createCancel(request, currentUserId)));
    }

    @Operation(summary = "판매 내역 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<SaleRecordPageResponse>> getList(
            @RequestParam Long creatorId,
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal Long currentUserId) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.of(
                saleRecordService.getList(creatorId, cursor, currentUserId, isAdmin)));
    }
}