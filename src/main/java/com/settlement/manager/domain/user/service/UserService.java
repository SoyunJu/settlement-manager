package com.settlement.manager.domain.user.service;

import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import com.settlement.manager.domain.user.dto.UserResponse;
import com.settlement.manager.domain.user.entity.User;
import com.settlement.manager.domain.user.repository.UserRepository;
import com.settlement.manager.infrastructure.audit.AuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    // currentUserId 검증
    @Transactional(readOnly = true)
    public UserResponse getMe(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    // @PreAuthorize 권한 제어
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    // 정산 이력 보존
    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DELETED);
        }
        user.softDelete();
        auditLogger.log("USER_DELETE", currentUserId, "targetUserId=" + userId);
        log.info("회원 탈퇴. userId={}", userId);
    }

    // 계정 잠금 해제
    @Transactional
    public void unlockUser(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!user.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_LOCKED);
        }
        user.unlock();
        auditLogger.log("USER_UNLOCK", currentUserId, "targetUserId=" + userId);
        log.info("계정 잠금 해제. userId={}", userId);
    }
}