package com.settlement.manager.domain.user.entity;

import com.settlement.manager.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users",
        indexes = @Index(name = "idx_users_email", columnList = "email"))
@org.hibernate.annotations.SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 로그인 실패 잠금 관련 필드
    private int loginFailCount;
    private Instant lockedAt;
    private Instant lastLoginFailedAt;

    @Builder
    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // 로그인 실패 기록. maxAttempts 도달 시 계정 잠금.
    public void recordLoginFailure(int maxAttempts) {
        this.loginFailCount++;
        this.lastLoginFailedAt = Instant.now();
        if (this.loginFailCount >= maxAttempts) {
            this.lockedAt = Instant.now();
        }
    }

    // 로그인 성공 시 실패 카운트 초기화
    public void recordLoginSuccess() {
        this.loginFailCount = 0;
        this.lastLoginFailedAt = null;
    }

    // 계정 잠금 해제 (OPERATOR 이상)
    public void unlock() {
        this.lockedAt = null;
        this.loginFailCount = 0;
        this.lastLoginFailedAt = null;
    }

    public boolean isLocked() { return lockedAt != null; }
}