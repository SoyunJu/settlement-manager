package com.settlement.manager.domain.user.service;

import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import com.settlement.manager.domain.user.dto.LoginRequest;
import com.settlement.manager.domain.user.entity.Role;
import com.settlement.manager.domain.user.entity.User;
import com.settlement.manager.domain.user.repository.UserRepository;
import com.settlement.manager.infrastructure.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxAttempts", 5);
        user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .role(Role.ROLE_CREATOR)
                .build();
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Test
    @DisplayName("로그인 성공 -> 토큰 반환 및 failCount 초기화")
    void login_success() {
        // 실패 카운트 미리 1 올려둠
        user.recordLoginFailure(10);

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtProvider.createAccessToken(any(), any())).willReturn("access-token");
        given(jwtProvider.createRefreshToken(any())).willReturn("refresh-token");
        given(jwtProvider.getRefreshExpiryMs()).willReturn(604800000L);

        authService.login(new LoginRequest("test@test.com", "password"));

        assertThat(user.getLoginFailCount()).isEqualTo(0);
        assertThat(user.getLastLoginFailedAt()).isNull();
    }

    @Test
    @DisplayName("로그인 실패 -> failCount 증가")
    void login_fail_increases_count() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@test.com", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        assertThat(user.getLoginFailCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("5회 실패 -> 계정 잠금")
    void login_fail_5_times_locks_account() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // 4회 실패
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> authService.login(new LoginRequest("test@test.com", "wrong")))
                    .isInstanceOf(BusinessException.class);
        }
        assertThat(user.isLocked()).isFalse();

        // 5회째 실패 -> Lock
        assertThatThrownBy(() -> authService.login(new LoginRequest("test@test.com", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);

        assertThat(user.isLocked()).isTrue();
        assertThat(user.getLoginFailCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("잠긴 계정 로그인 시도 -> 예외 처리")
    void locked_account_cannot_login() {

        for (int i = 0; i < 5; i++) {
            user.recordLoginFailure(5);
        }
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@test.com", "password")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    @DisplayName("탈퇴한 계정 로그인 시도 -> 예외 처리")
    void deleted_account_cannot_login() {
        user.softDelete();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@test.com", "password")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_ALREADY_DELETED);
    }

    @Test
    @DisplayName("로그아웃 - 토큰 무효화")
    void logout_deletes_refresh_token() {
        authService.logout(1L);
        verify(redisTemplate).delete("refresh:1");
    }
}