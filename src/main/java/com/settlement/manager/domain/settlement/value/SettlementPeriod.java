package com.settlement.manager.domain.settlement.value;

import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;

import java.time.*;
import java.time.format.DateTimeParseException;

// KST 기준 월 경계 변환
public record SettlementPeriod(YearMonth yearMonth) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // KST 기준 월 시작 (1일 00:00:00 KST)
    public Instant startInstant() {
        return yearMonth.atDay(1).atStartOfDay(KST).toInstant();
    }

    // KST 기준 월 종료 (말일 23:59:59.999999999 KST)
    public Instant endInstant() {
        return yearMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(KST).toInstant();
    }

    public int year() { return yearMonth.getYear(); }
    public int month() { return yearMonth.getMonthValue(); }

    public static SettlementPeriod of(String yearMonth) {
        try {
            return new SettlementPeriod(YearMonth.parse(yearMonth));
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
    }

    // 미래 여부 확인
    public boolean isFuture() {
        return yearMonth.isAfter(YearMonth.now(KST));
    }
}