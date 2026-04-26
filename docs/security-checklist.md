# OWASP Top 10 보안 점검 체크리스트

## A01 - 접근 제어 취약점
- [x] @PreAuthorize ROLE 기반 API 접근 제어
- [x] IDOR 방지: creatorId == currentUserId 검증 (SaleRecord, Settlement, CreatorStats)
- [x] Actuator 엔드포인트 화이트리스트 (health, info만 허용)
- [x] RoleHierarchy: ADMIN > OPERATOR > CREATOR

## A02 - 암호화 실패
- [x] BCrypt strength=12 비밀번호 해싱
- [x] JWT Secret 환경변수 관리 (.env, 절대 하드코딩 금지)
- [x] HTTPS 강제 (HSTS 헤더, prod 환경)

## A03 - 인젝션
- [x] Spring Data JPA + JPQL 파라미터 바인딩 (Native Query 미사용)
- [x] @Valid 입력값 검증 (DTO 레벨)

## A04 - 안전하지 않은 설계
- [x] SettlementPeriod 값 객체로 경계값 캡슐화
- [x] 상태 전이 엔티티 레벨 강제 (confirm/pay 역행 불가)

## A05 - 보안 설정 오류
- [x] X-Content-Type-Options: nosniff
- [x] X-Frame-Options: DENY
- [x] HSTS 1년
- [x] Referrer-Policy: no-referrer
- [x] CORS 허용 Origin 명시 (와일드카드 금지)

## A07 - 인증 및 인증 실패
- [x] 로그인 5회 실패 계정 잠금
- [x] RefreshToken Redis 저장 + 로그아웃 즉시 폐기
- [x] Bucket4j Rate Limiting (로그인 30분/5회)
- [x] JWT 만료/위변조 검증

## A09 - 보안 로깅 및 모니터링 실패
- [x] MDC traceId/userId/method/uri 전 요청 추적
- [x] AuditLogger: 정산 상태 변경, 회원 삭제, 수수료 변경 감사 로그
- [x] 500 에러: 스택 트레이스 로그에만, body 최소 노출
- [x] 도메인별 로그 파일 분리 (settlement/sale/admin/audit)