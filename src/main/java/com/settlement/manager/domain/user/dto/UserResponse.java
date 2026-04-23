package com.settlement.manager.domain.user.dto;

import com.settlement.manager.domain.user.entity.Role;
import com.settlement.manager.domain.user.entity.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean locked,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isLocked(),
                user.getCreatedAt()
        );
    }
}