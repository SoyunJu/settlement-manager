package com.settlement.manager.api.v1;

import com.settlement.manager.common.response.ApiResponse;
import com.settlement.manager.domain.user.dto.UserResponse;
import com.settlement.manager.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User", description = "회원 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.of(userService.getMe(userId)));
    }

    @Operation(summary = "전체 회원 목록 (ADMIN 전용)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.of(userService.getAll()));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long currentUserId) {
        userService.deleteUser(userId, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "계정 잠금 해제 (OPERATOR 이상)")
    @PostMapping("/admin/users/{userId}/unlock")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long currentUserId) {
        userService.unlockUser(userId, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}