package com.settlement.manager.infrastructure.batch;

import com.settlement.manager.domain.settlement.entity.Settlement;
import com.settlement.manager.domain.settlement.repository.SettlementRepository;
import com.settlement.manager.domain.settlement.service.SettlementCalculator;
import com.settlement.manager.domain.settlement.value.SettlementPeriod;
import com.settlement.manager.domain.user.entity.Role;
import com.settlement.manager.domain.user.entity.User;
import com.settlement.manager.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonthlySettlementJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final SettlementCalculator settlementCalculator;
    private final SettlementRepository settlementRepository;

    private static final int CHUNK_SIZE = 100;
    private static final int SKIP_LIMIT = 10;

    @Bean
    public Job monthlySettlementJob() {
        return new JobBuilder("monthlySettlementJob", jobRepository)
                .start(monthlySettlementStep())
                .build();
    }

    @Bean
    public Step monthlySettlementStep() {
        return new StepBuilder("monthlySettlementStep", jobRepository)
                .<User, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(creatorReader())
                .processor(settlementProcessor(null))
                .writer(settlementWriter())
                // 중복 정산 등 예외 발생 시 skip 후 로그 기록 (skipLimit=10)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(SKIP_LIMIT)
                .listener(skipListener())
                .build();
    }

    @Bean
    public ListItemReader<User> creatorReader() {
        List<User> creators = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_CREATOR && !u.isDeleted())
                .toList();
        log.info("월별 정산 대상 크리에이터 수: {}", creators.size());
        return new ListItemReader<>(creators);
    }

    @Bean
    public ItemProcessor<User, Settlement> settlementProcessor(
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['yearMonth']}") String yearMonth) {
        return user -> {
            if (yearMonth == null) {
                log.warn("yearMonth 파라미터 없음. creatorId={}", user.getId());
                return null;
            }
            SettlementPeriod period = SettlementPeriod.of(yearMonth);

            if (settlementRepository.existsByCreatorIdAndYearAndMonth(
                    user.getId(), period.year(), period.month())) {
                log.info("정산 이미 존재. skip. creatorId={} period={}", user.getId(), yearMonth);
                return null;
            }
            return settlementCalculator.calculate(user.getId(), user.getGrade(), period);
        };
    }

    @Bean
    public ItemWriter<Settlement> settlementWriter() {
        return chunk -> {
            settlementRepository.saveAll(chunk.getItems());
            log.info("정산 저장 완료. count={}", chunk.getItems().size());
        };
    }

    @Bean
    public SkipListener<User, Settlement> skipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInProcess(User user, Throwable t) {
                log.error("정산 skip. creatorId={} reason={}", user.getId(), t.getMessage());
            }
        };
    }
}