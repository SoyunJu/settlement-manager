package com.settlement.manager.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Profile("prod") // 로컬 실행 방지
@RequiredArgsConstructor
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 전월 정산 일괄 실행
    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public void runMonthlySettlement() {

        LocalDate lastMonth = LocalDate.now(KST).minusMonths(1);
        String yearMonth = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("yearMonth", yearMonth)
                    .addString("runAt", LocalDateTime.now().toString())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(monthlySettlementJob, params);
            log.info("전월 정산 완료. yearMonth={} status={}", yearMonth, execution.getStatus());
        } catch (Exception e) {
            log.error("전월 정산 실패. yearMonth={}", yearMonth, e);
        }
    }
}