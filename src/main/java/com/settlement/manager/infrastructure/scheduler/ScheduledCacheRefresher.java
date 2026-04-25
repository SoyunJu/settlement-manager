package com.settlement.manager.infrastructure.scheduler;

import com.settlement.manager.domain.settlement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Profile("prod") // 로컬 실행 방지
@RequiredArgsConstructor
public class ScheduledCacheRefresher {

    private final DashboardService dashboardService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void refreshDashboardCache() {
        LocalDate now = LocalDate.now(KST);
        String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String lastMonth = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        log.info("정산 캐시 갱신 시작. currentMonth={} lastMonth={}", currentMonth, lastMonth);
        dashboardService.refreshCache(lastMonth);
        dashboardService.refreshCache(currentMonth);
        log.info("정산 캐시 갱신 완료.");
    }
}