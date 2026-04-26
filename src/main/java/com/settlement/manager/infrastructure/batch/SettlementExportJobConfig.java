package com.settlement.manager.infrastructure.batch;

import com.settlement.manager.domain.settlement.entity.Settlement;
import com.settlement.manager.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.*;
import java.nio.file.*;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementExportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SettlementRepository settlementRepository;

    private static final int CHUNK_SIZE = 100;
    private static final String EXPORT_DIR = "logs/export";

    @Bean
    public Job settlementExportJob() {
        return new JobBuilder("settlementExportJob", jobRepository)
                .start(csvExportStep())
                .next(excelExportStep())
                .build();
    }

    // --- CSV ---

    @Bean
    public Step csvExportStep() {
        return new StepBuilder("csvExportStep", jobRepository)
                .<Settlement, String>chunk(CHUNK_SIZE, transactionManager)
                .reader(exportReader(null))
                .processor(csvProcessor())
                .writer(csvWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<Settlement> exportReader(
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['yearMonth']}") String yearMonth) {
        if (yearMonth == null) return new ListItemReader<>(List.of());
        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        List<Settlement> settlements = settlementRepository.findByYearAndMonth(year, month);
        log.info("CSV/Excel 추출 대상: {} 건 (period={})", settlements.size(), yearMonth);
        return new ListItemReader<>(settlements);
    }

    @Bean
    public ItemProcessor<Settlement, String> csvProcessor() {
        return settlement -> String.join(",",
                String.valueOf(settlement.getId()),
                String.valueOf(settlement.getCreatorId()),
                settlement.getYear() + "-" + String.format("%02d", settlement.getMonth()),
                settlement.getTotalSaleAmount().toPlainString(),
                settlement.getTotalRefundAmount().toPlainString(),
                settlement.getNetSaleAmount().toPlainString(),
                settlement.getFeeRate().toPlainString(),
                settlement.getFeeAmount().toPlainString(),
                settlement.getSettlementAmount().toPlainString(),
                settlement.getStatus().name()
        );
    }

    @Bean
    @StepScope
    public ItemWriter<String> csvWriter(
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['yearMonth']}") String yearMonth) {
        return chunk -> {
            String period = (yearMonth != null) ? yearMonth : "unknown";
            Path dir = Paths.get(EXPORT_DIR);
            Files.createDirectories(dir);
            Path file = dir.resolve("settlement-" + period + ".csv");

            try (BufferedWriter writer = Files.newBufferedWriter(file,
                    java.nio.charset.StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                if (!Files.exists(file) || Files.size(file) == 0) {
                    writer.write("id,creatorId,period,totalSale,totalRefund,netSale,feeRate,feeAmount,settlementAmount,status");
                    writer.newLine();
                }
                for (String line : chunk.getItems()) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            log.info("CSV 청크 저장. file={} count={}", file, chunk.getItems().size());
        };
    }

    // --- Excel ---

    @Bean
    public Step excelExportStep() {
        return new StepBuilder("excelExportStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(exportReader(null))
                .processor(item -> item)
                .writer(excelWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<Settlement> excelWriter(
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['yearMonth']}") String yearMonth) {
        return chunk -> {
            String period = (yearMonth != null) ? yearMonth : "unknown";
            Path dir = Paths.get(EXPORT_DIR);
            Files.createDirectories(dir);
            Path file = dir.resolve("settlement-" + period + ".xlsx");

            // upsert
            Workbook workbook;
            Sheet sheet;
            int startRow;

            if (Files.exists(file)) {
                try (InputStream is = Files.newInputStream(file)) {
                    workbook = new XSSFWorkbook(is);
                    sheet = workbook.getSheetAt(0);
                    startRow = sheet.getLastRowNum() + 1;
                }
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("정산내역");
                startRow = 0;
                // 헤더
                Row header = sheet.createRow(0);
                String[] headers = {"ID", "크리에이터ID", "기간", "총판매", "총환불", "순판매", "수수료율", "수수료", "정산금액", "상태"};
                for (int i = 0; i < headers.length; i++) {
                    header.createCell(i).setCellValue(headers[i]);
                }
                startRow = 1;
            }

            for (Settlement s : chunk.getItems()) {
                Row row = sheet.createRow(startRow++);
                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getCreatorId());
                row.createCell(2).setCellValue(s.getYear() + "-" + String.format("%02d", s.getMonth()));
                row.createCell(3).setCellValue(s.getTotalSaleAmount().doubleValue());
                row.createCell(4).setCellValue(s.getTotalRefundAmount().doubleValue());
                row.createCell(5).setCellValue(s.getNetSaleAmount().doubleValue());
                row.createCell(6).setCellValue(s.getFeeRate().doubleValue());
                row.createCell(7).setCellValue(s.getFeeAmount().doubleValue());
                row.createCell(8).setCellValue(s.getSettlementAmount().doubleValue());
                row.createCell(9).setCellValue(s.getStatus().name());
            }

            try (OutputStream os = Files.newOutputStream(file)) {
                workbook.write(os);
            }
            workbook.close();
            log.info("Excel 청크 저장. file={} count={}", file, chunk.getItems().size());
        };
    }
}