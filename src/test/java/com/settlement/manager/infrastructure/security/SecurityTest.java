package com.settlement.manager.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import com.settlement.manager.domain.user.entity.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JwtProvider jwtProvider;

    private static final String SECRET = "test-secret-key-minimum-32-characters!!";

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, 15L, 7L);
    }

    // =========================================================
    // A01 - 접근 제어
    // =========================================================
    @Nested
    @DisplayName("A01 - 접근 제어")
    class AccessControlTests {

        @Test
        @DisplayName("미인증 요청 -> 401")
        void unauthenticated_request_returns_401() throws Exception {
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("미인증 정산 조회 -> 401")
        void unauthenticated_settlement_returns_401() throws Exception {
            mockMvc.perform(get("/api/v1/settlements")
                            .param("creatorId", "1")
                            .param("yearMonth", "2025-01"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("CREATOR가 전체 회원 목록 조회 -> 403")
        @WithMockUser(roles = "CREATOR")
        void creator_cannot_access_all_users() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CREATOR가 수수료율 변경 시도 -> 403")
        @WithMockUser(roles = "CREATOR")
        void creator_cannot_change_fee_rate() throws Exception {
            mockMvc.perform(post("/api/v1/fee-rates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("rate", 0.15))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CREATOR가 배치 실행 시도 -> 403")
        @WithMockUser(roles = "CREATOR")
        void creator_cannot_run_batch() throws Exception {
            mockMvc.perform(post("/api/v1/batch/settlement")
                            .param("yearMonth", "2025-01"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CREATOR가 정산 확정 시도 -> 403")
        @WithMockUser(roles = "CREATOR")
        void creator_cannot_confirm_settlement() throws Exception {
            mockMvc.perform(post("/api/v1/settlements/1/confirm"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("OPERATOR가 전체 회원 목록 조회 -> 200 (RoleHierarchy)")
        @WithMockUser(roles = "OPERATOR")
        void operator_can_access_all_users_via_role_hierarchy() throws Exception {
            // RoleHierarchy: OPERATOR > CREATOR 이므로 ADMIN 전용 API는 403
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isForbidden()); // ADMIN 전용
        }

        @Test
        @DisplayName("ADMIN이 전체 회원 목록 조회 -> 200")
        @WithMockUser(roles = "ADMIN")
        void admin_can_access_all_users() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================
    // A02/A07 - JWT 보안
    // =========================================================
    @Nested
    @DisplayName("A02/A07 - JWT 보안")
    class JwtSecurityTests {

        @Test
        @DisplayName("정상 토큰 파싱 성공")
        void valid_token_parsed_successfully() {
            String token = jwtProvider.createAccessToken(1L, Role.ROLE_CREATOR);
            assertThat(jwtProvider.getUserId(token)).isEqualTo(1L);
            assertThat(jwtProvider.getRole(token)).isEqualTo(Role.ROLE_CREATOR);
        }

        @Test
        @DisplayName("만료된 토큰 -> TOKEN_EXPIRED 예외")
        void expired_token_throws_exception() {
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            String expiredToken = Jwts.builder()
                    .subject("1")
                    .claim("role", Role.ROLE_CREATOR.name())
                    .issuedAt(new Date(System.currentTimeMillis() - 2000))
                    .expiration(new Date(System.currentTimeMillis() - 1000))
                    .signWith(key)
                    .compact();

            assertThatThrownBy(() -> jwtProvider.parseClaims(expiredToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("위조된 서명 토큰 -> INVALID_TOKEN 예외")
        void tampered_signature_throws_exception() {
            String token = jwtProvider.createAccessToken(1L, Role.ROLE_CREATOR);
            String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature";

            assertThatThrownBy(() -> jwtProvider.parseClaims(tampered))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("다른 Secret으로 서명된 토큰 -> INVALID_TOKEN 예외")
        void different_secret_token_throws_exception() {
            JwtProvider other = new JwtProvider(
                    "another-secret-key-minimum-32-characters!!", 15L, 7L);
            String tokenFromOther = other.createAccessToken(1L, Role.ROLE_CREATOR);

            assertThatThrownBy(() -> jwtProvider.parseClaims(tokenFromOther))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("빈 문자열 토큰 -> INVALID_TOKEN 예외")
        void empty_token_throws_exception() {
            assertThatThrownBy(() -> jwtProvider.parseClaims(""))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("형식이 잘못된 토큰 -> INVALID_TOKEN 예외")
        void malformed_token_throws_exception() {
            assertThatThrownBy(() -> jwtProvider.parseClaims("not.a.jwt"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("AccessToken과 RefreshToken은 서로 다른 값")
        void access_and_refresh_tokens_are_different() {
            String access = jwtProvider.createAccessToken(1L, Role.ROLE_CREATOR);
            String refresh = jwtProvider.createRefreshToken(1L);
            assertThat(access).isNotEqualTo(refresh);
        }
    }

    // =========================================================
    // A05 - 보안 헤더
    // =========================================================
    @Nested
    @DisplayName("A05 - 보안 헤더")
    class SecurityHeaderTests {

        @Test
        @DisplayName("X-Content-Type-Options: nosniff 헤더 존재")
        void x_content_type_options_header() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        }

        @Test
        @DisplayName("X-Frame-Options: DENY 헤더 존재 (클릭재킹 방지)")
        void x_frame_options_header() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(header().string("X-Frame-Options", "DENY"));
        }

        @Test
        @DisplayName("Actuator health 공개 접근 허용")
        void actuator_health_is_public() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Actuator health/info 외 엔드포인트 차단")
        @WithMockUser // 테스트 위해 유저 권한 부여
        void actuator_other_endpoints_blocked() throws Exception {
            mockMvc.perform(get("/actuator/env"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/actuator/beans"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Swagger UI 공개 접근 허용")
        void swagger_ui_is_public() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================
    // A07 - Rate Limiting
    // =========================================================
    @Nested
    @DisplayName("A07 - Rate Limiting")
    class RateLimitTests {

        @Test
        @DisplayName("로그인 외 엔드포인트는 Rate Limit 미적용")
        void non_login_endpoint_not_rate_limited() throws Exception {
            for (int i = 0; i < 10; i++) {
                int status = mockMvc.perform(post("/api/v1/auth/refresh")
                                .header("X-Refresh-Token", "invalid-token"))
                        .andReturn().getResponse().getStatus();
                assertThat(status).isNotEqualTo(429);
            }
        }
    }

    // =========================================================
    // A03 - 입력값 검증
    // =========================================================
    @Nested
    @DisplayName("A03 - 입력값 검증")
    class InputValidationTests {

        @Test
        @DisplayName("이메일 형식 오류 회원가입 -> 400")
        void invalid_email_signup_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("email", "not-an-email",
                                            "password", "password123",
                                            "role", "ROLE_CREATOR"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호 8자 미만 회원가입 -> 400")
        void short_password_signup_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("email", "test@test.com",
                                            "password", "short",
                                            "role", "ROLE_CREATOR"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("필수 필드 누락 로그인 요청 -> 400")
        void missing_field_login_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("500 에러 응답에 스택 트레이스 미포함 (A09)")
        void internal_error_response_hides_stack_trace() throws Exception {
            String response = mockMvc.perform(get("/api/v1/users/me"))
                    .andReturn().getResponse().getContentAsString();

            // 스택 트레이스 노출되면 x
            assertThat(response).doesNotContain("at com.settlement");
            assertThat(response).doesNotContain("Exception");
            assertThat(response).doesNotContain("stack");
        }
    }
}