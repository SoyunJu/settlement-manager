# Settlement Manager — 크리에이터 정산 관리 시스템

## 프로젝트 개요

크리에이터가 강의를 판매하고, 플랫폼이 수수료를 제하여 월별 정산하는 내부 어드민 시스템입니다.

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.3.0 |
| 빌드 | Gradle 8.7 (Kotlin DSL) |
| DB | PostgreSQL 16 |
| 캐시 | Redis 7 |
| 인증 | JWT (AccessToken 15분 + RefreshToken 7일) |
| 배치 | Spring Batch 5 |
| 문서 | Springdoc OpenAPI (Swagger UI) |
| 인프라 | Docker Compose |
| 프론트엔드 | React 18 + TypeScript + Vite |

---

## 로컬 실행 방법

### 사전 조건
- Java 17
- Docker Desktop (실행 중)
- Node.js (프론트엔드)

### 백엔드

```bash
# 1. 환경변수 파일 생성
cp .env.example .env
# .env 열어서 DB_PASSWORD, REDIS_PASSWORD, JWT_SECRET 입력

# 2. Docker 컨테이너 실행 (PostgreSQL 5532, Redis 6479)
docker compose up -d

# 3. 빌드 확인
./gradlew build -x test

# 4. 실행 (IntelliJ 또는 명령줄)
./gradlew bootRun
```

### 프론트엔드

```bash
cd client
npm install
npm run dev
# http://localhost:3100 접속
```

### 접속 정보

| 서비스 | 주소 |
|--------|------|
| Backend | http://localhost:8180 |
| Swagger UI | http://localhost:8180/swagger-ui/index.html |
| Frontend | http://localhost:3100 |

---

## 역할(Role) 및 권한

| Role | 주요 권한 |
|------|-----------|
| ROLE_CREATOR | 본인 정산·판매 내역 조회, 취소 내역 등록 |
| ROLE_OPERATOR | 전체 정산 집계, 대시보드, 정산 확정/지급 처리, 계정 잠금 해제 |
| ROLE_ADMIN | 수수료율 변경, 전체 회원 관리, 배치 실행, 모든 OPERATOR 권한 |

RoleHierarchy: `ROLE_ADMIN > ROLE_OPERATOR > ROLE_CREATOR`

---

## 구현 기능

### 필수 구현

**판매 내역 관리**
- 판매 내역 등록 (강의 ID, 수강생 ID, 결제 금액, 결제 일시)
- 취소 내역 등록 (원본 판매 내역 참조, 환불 금액 검증: 환불액 ≤ 원결제액)
- 판매 내역 목록 조회 (크리에이터별, 커서 기반 페이지네이션)

**정산 금액 계산 API**
- 크리에이터별 월별 정산 조회 (creatorId + yearMonth)
- 응답: 총 판매금액 / 환불금액 / 순 판매금액 / 수수료 / 정산 예정금액 / 판매·취소 건수
- KST 기준 월 경계 처리 (1일 00:00:00 ~ 말일 23:59:59 KST)

**정산 내역 집계 API (운영자용)**
- 월별 전체 크리에이터 정산 현황 집계
- 크리에이터별 정산 예정금액 목록 + 전체 합계

### 선택 구현

- **정산 상태 관리**: `PENDING → CONFIRMED → PAID` (역행 불가)
- **동시 확정 방지**: 비관적 락(PESSIMISTIC_WRITE) 적용
- **중복 정산 방지**: (creatorId, year, month) UNIQUE 제약 + 서비스 레이어 검증
- **엑셀/CSV 다운로드**: Spring Batch 기반 청크 처리 → `logs/export/` 저장
- **수수료율 변경 이력**: FeeRate 이력 테이블 + 정산 시점 기준 율 조회 (FeeRateResolver)
- **등급별 수수료 override**: BRONZE / SILVER / GOLD / PLATINUM

---

## 정산 기간 기준

```
결제 완료 일시 기준 / 취소는 취소 일시 기준
월 경계: KST 기준 해당 월 1일 00:00:00 ~ 말일 23:59:59

구현 방법:
- SaleRecord.paidAt: TIMESTAMPTZ (UTC 저장, 계산 기준)
- SaleRecord.paidAtKst: TIMESTAMP (KST 변환값, DB 육안 확인용)
- SettlementPeriod 값 객체가 KST 기준 Instant 범위를 계산
```

**경계값 예시**

| 결제 일시 (KST) | 귀속 월 |
|----------------|---------|
| 2025-01-31 23:30 KST | 2025년 1월 |
| 2025-02-01 00:00 KST | 2025년 2월 |
| 2025-03-31 23:59:59 KST | 2025년 3월 |

**케이스 5 처리 방식**
`sale-5` (2025-01-31 23:30 KST 결제) → **1월 판매**로 귀속
`cancel-3` (2025-02-xx 취소) → **2월 취소**로 귀속
두 트랜잭션은 각각 해당 월 정산에 독립적으로 반영됩니다.

---

## 샘플 데이터 검증 시나리오

| 시나리오 | 관련 데이터 | 확인 포인트 |
|----------|------------|-------------|
| creator-1의 2025-03 정산 | sale-1,2,3,4 + cancel-1,2 | 총판매 260,000 / 환불 110,000 / 순판매 150,000 / 수수료 30,000 / 정산예정 120,000 |
| 부분 환불 처리 | sale-4, cancel-2 | 환불액(30,000) < 원결제(80,000) → 순판매에 정상 반영 |
| 월 경계 취소 | sale-5, cancel-3 | sale-5는 1월 판매, cancel-3은 2월 취소로 각각 독립 반영 |
| 빈 월 조회 | creator-3, 2025-03 | 판매 내역 없는 월 → 0원 정상 응답 |

### 추가 검증 케이스

| 케이스 | 추가 이유 |
|--------|-----------|
| 미래 연월 요청 (2099-01) | 미래 정산 조회 방지 로직 검증 (`FUTURE_PERIOD_NOT_ALLOWED`) |
| 잘못된 형식 (2025/03, 202503) | 입력값 검증 로직 확인 (`INVALID_PERIOD_FORMAT`) |
| 동시 확정 10스레드 요청 | 비관적 락으로 1건만 성공, 나머지 예외 처리 확인 |
| 환불액 > 원결제액 요청 | `REFUND_EXCEEDS_ORIGINAL` 예외 정상 반환 확인 |
| 5회 로그인 실패 후 재시도 | 계정 잠금 및 `ACCOUNT_LOCKED` 응답 확인 |
| 동일 기간 정산 재요청 | 중복 생성 없이 기존 정산 반환 확인 |

---

## API 주요 엔드포인트

| 메서드 | 경로 | 권한 | 설명 |
|--------|------|------|------|
| POST | /api/v1/auth/signup | 공개 | 회원가입 |
| POST | /api/v1/auth/login | 공개 | 로그인 |
| POST | /api/v1/sale-records | ADMIN | 판매 내역 등록 |
| POST | /api/v1/sale-records/cancel | 인증 | 취소 내역 등록 |
| GET | /api/v1/sale-records | 인증 | 판매 내역 목록 |
| GET | /api/v1/settlements | 인증 | 월별 정산 조회 |
| POST | /api/v1/settlements/{id}/confirm | OPERATOR+ | 정산 확정 |
| POST | /api/v1/settlements/{id}/pay | OPERATOR+ | 지급 완료 |
| GET | /api/v1/dashboard | OPERATOR+ | 운영자 집계 대시보드 |
| POST | /api/v1/batch/settlement | ADMIN | 월별 일괄 정산 배치 실행 |
| POST | /api/v1/batch/export | ADMIN | CSV/Excel 추출 |
| GET | /api/v1/fee-rates/current | 인증 | 현재 수수료율 조회 |
| POST | /api/v1/fee-rates | ADMIN | 수수료율 변경 |

전체 API 목록은 Swagger UI에서 확인: `http://localhost:8180/swagger-ui/index.html`

---

## 테스트

```bash
./gradlew test

# 리포트
open build/reports/tests/test/index.html
```

| 테스트 파일 | 유형 | 주요 검증 |
|-------------|------|-----------|
| SettlementPeriodTest | 단위 | KST 월 경계값 (말일 23:30, 윤년 2월, 미래 월 감지) |
| SettlementCalculatorTest | 단위 | 정산 계산 (정상/빈 월/부분 환불/소수점 반올림) |
| FeeRateResolverTest | 단위 | 등급별 override, DB 없을 때 env 폴백 |
| AuthServiceTest | 단위 | 로그인 5회 잠금, 탈퇴 계정, 로그아웃 Redis 삭제 |
| SaleRecordServiceTest | 단위 | IDOR 방지, 환불 초과 예외, 전액 환불 허용 |
| SettlementIntegrationTest | 통합 | 실 DB 기반 정산 생성, 중복 방지 |
| SettlementConcurrencyTest | 통합 | 동시 확정 10스레드 — 1번만 성공 |
| SecurityTest | 통합 | JWT 위변조/만료, 접근제어, 보안 헤더 |

---

## 보안 (OWASP Top 10)

| 항목 | 적용 |
|------|------|
| A01 접근 제어 | @PreAuthorize 역할 기반 제어, IDOR 방지 (creatorId == currentUserId 검증) |
| A02 암호화 | BCrypt strength=12, JWT Secret 환경변수 관리 (최소 32자) |
| A03 인젝션 | JPA JPQL + 파라미터 바인딩만 사용 |
| A05 보안 헤더 | X-Frame-Options: DENY, X-Content-Type-Options: nosniff, HSTS 1년 |
| A07 인증 실패 | 로그인 5회 실패 계정 잠금, RefreshToken 로그아웃 즉시 폐기, Rate Limiting |
| A09 로깅 | MDC traceId/userId/method/uri 전 요청 추적, 감사 로그 1년 보관 |

---

## 주요 설계 결정

| 결정 | 이유 |
|------|------|
| paidAt(TIMESTAMPTZ) + paidAtKst(TIMESTAMP) 병행 | UTC로 계산 정확성 보장. KST 컬럼은 DB 관리자 육안 확인 전용. 계산에 미사용. |
| SettlementPeriod 값 객체 | KST 월 경계 계산 로직 캡슐화. 경계값 단위 테스트 독립 실행 가능. |
| 비관적 락 (PESSIMISTIC_WRITE) | 정산 확정 동시 요청 방지. 낙관적 락 대비 재시도 로직 불필요. |
| UNIQUE 제약 + 서비스 레이어 중복 체크 병행 | DB 제약은 최후 방어선. 서비스 레이어에서 먼저 체크해 명확한 에러 코드 반환. |
| FeeRate 이력 테이블 + 시점 기준 조회 | 수수료율 변경 후에도 과거 정산은 당시 수수료율로 재계산 가능. |
| Soft Delete | 정산 이력과 연결된 User 하드 삭제 불가. deletedAt 기록으로 이력 보존. |

---

## AI 활용 범위

| 영역 | AI 활용도 | 직접 판단 영역 |
|------|-----------|---------------|
| 엔티티·DTO 스캐폴딩 | 높음 | 필드 설계, KST 컬럼 병행 여부 결정 |
| CRUD API 기본 구조 | 높음 | IDOR 검증 위치, 응답 구조 설계 |
| KST 월 경계 처리 | 낮음 | TIMESTAMPTZ + paidAtKst 병행 설계 근거 직접 판단 |
| 정산 계산 로직 | 낮음 | 수수료율 시점 조회 기준, FeeRateResolver 설계 직접 판단 |
| 정산 중복 방지 | 낮음 | UNIQUE 제약 + 비관적 락 조합 직접 결정 |
| 로그인 잠금 정책 | 중간 | 잠금 해제 권한 범위, 카운트 초기화 시점 직접 결정 |
| Spring Batch 설계 | 낮음 | Job 구조, 청크 크기, skip 정책 직접 결정 |
| 테스트 시나리오 | 낮음 | 경계값·동시성·IDOR 시나리오 직접 정의 |
| 보안 설정 | 중간 | OWASP 항목별 적용 여부 직접 검토·확인 |
| React UI 구조 | 높음 | DDD 기반 컴포넌트 책임 분리 방식 직접 결정 |