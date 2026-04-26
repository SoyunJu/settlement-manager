package com.settlement.manager.api.v1;

import com.settlement.manager.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Batch", description = "정산 배치 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;
    private final Job settlementExportJob;

    @Operation(summary = "월별 일괄 정산 실행 (ADMIN 전용)")
    @PostMapping("/settlement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> runMonthlySettlement(
            @RequestParam String yearMonth) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("yearMonth", yearMonth)
                .addString("runAt", LocalDateTime.now().toString()) // 동일 파라미터 중복 실행 방지
                .toJobParameters();

        JobExecution execution = jobLauncher.run(monthlySettlementJob, params);
        log.info("월별 정산 배치 실행. yearMonth={} status={}", yearMonth, execution.getStatus());
        return ResponseEntity.ok(ApiResponse.of("배치 실행: " + execution.getStatus()));
    }

    @Operation(summary = "정산 추출 (ADMIN 전용)")
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> runExport(
            @RequestParam String yearMonth) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("yearMonth", yearMonth)
                .addString("runAt", LocalDateTime.now().toString())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(settlementExportJob, params);
        log.info("정산 추출 배치 실행. yearMonth={} status={}", yearMonth, execution.getStatus());
        return ResponseEntity.ok(ApiResponse.of("추출 배치 실행: " + execution.getStatus()
                + " | 파일 위치: logs/export/settlement-" + yearMonth + ".csv / .xlsx"));
    }
}