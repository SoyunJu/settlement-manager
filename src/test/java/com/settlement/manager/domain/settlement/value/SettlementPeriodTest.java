package com.settlement.manager.domain.settlement.value;

import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

class SettlementPeriodTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("1월 31일 23:30 KST -> 1월에 포함")
    void jan31_2330_kst_belongs_to_january() {
        SettlementPeriod period = SettlementPeriod.of("2025-01");
        Instant jan31_2330_kst = LocalDateTime.of(2025, 1, 31, 23, 30)
                .atZone(KST).toInstant();

        assertThat(jan31_2330_kst).isAfterOrEqualTo(period.startInstant());
        assertThat(jan31_2330_kst).isBeforeOrEqualTo(period.endInstant());
    }

    @Test
    @DisplayName("2월 1일 00:00 KST -> 2월에 포함")
    void feb1_0000_kst_not_in_january() {
        SettlementPeriod period = SettlementPeriod.of("2025-01");
        Instant feb1_0000_kst = LocalDateTime.of(2025, 2, 1, 0, 0)
                .atZone(KST).toInstant();

        assertThat(feb1_0000_kst).isAfter(period.endInstant());
    }

    @Test
    @DisplayName("1월 1일 00:00 KST -> 1월에 포함")
    void jan1_0000_kst_belongs_to_january() {
        SettlementPeriod period = SettlementPeriod.of("2025-01");
        Instant jan1_0000_kst = LocalDateTime.of(2025, 1, 1, 0, 0)
                .atZone(KST).toInstant();

        assertThat(jan1_0000_kst).isAfterOrEqualTo(period.startInstant());
        assertThat(jan1_0000_kst).isBeforeOrEqualTo(period.endInstant());
    }

    @Test
    @DisplayName("12월 31일 23:59 KST -> 12월에 포함")
    void dec31_2359_kst_belongs_to_december() {
        SettlementPeriod period = SettlementPeriod.of("2025-12");
        Instant dec31_2359_kst = LocalDateTime.of(2025, 12, 31, 23, 59)
                .atZone(KST).toInstant();

        assertThat(dec31_2359_kst).isAfterOrEqualTo(period.startInstant());
        assertThat(dec31_2359_kst).isBeforeOrEqualTo(period.endInstant());
    }

    @Test
    @DisplayName("미래 월 -> True")
    void future_period_is_detected() {
        YearMonth futureMonth = YearMonth.now(KST).plusMonths(1);
        SettlementPeriod period = SettlementPeriod.of(futureMonth.toString());

        assertThat(period.isFuture()).isTrue();
    }

    @Test
    @DisplayName("현재 월 -> False")
    void current_period_is_not_future() {
        YearMonth currentMonth = YearMonth.now(KST);
        SettlementPeriod period = SettlementPeriod.of(currentMonth.toString());

        assertThat(period.isFuture()).isFalse();
    }

    @Test
    @DisplayName("잘못된 형식 -> 예외")
    void invalid_format_throws_exception() {
        assertThatThrownBy(() -> SettlementPeriod.of("2025/01"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PERIOD_FORMAT);

        assertThatThrownBy(() -> SettlementPeriod.of("202501"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PERIOD_FORMAT);

        assertThatThrownBy(() -> SettlementPeriod.of("2025-13"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PERIOD_FORMAT);
    }

    @Test
    @DisplayName("KST Instance start<end 검증")
    void start_is_before_end() {
        SettlementPeriod period = SettlementPeriod.of("2025-02");
        assertThat(period.startInstant()).isBefore(period.endInstant());
    }

    @Test
    @DisplayName("윤년 2월 -> 2월에 포함")
    void leap_year_february_end() {
        SettlementPeriod period = SettlementPeriod.of("2024-02");
        Instant feb29_2359_kst = LocalDateTime.of(2024, 2, 29, 23, 59)
                .atZone(KST).toInstant();

        assertThat(feb29_2359_kst).isBeforeOrEqualTo(period.endInstant());
    }
}