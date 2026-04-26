package com.settlement.manager.domain.settlement.service;

import com.settlement.manager.domain.feerate.entity.FeeRate;
import com.settlement.manager.domain.feerate.repository.FeeRateRepository;
import com.settlement.manager.domain.settlement.dto.SettlementResponse;
import com.settlement.manager.domain.settlement.entity.Settlement;
import com.settlement.manager.domain.settlement.entity.SettlementStatus;
import com.settlement.manager.domain.settlement.repository.SettlementRepository;
import com.settlement.manager.domain.user.entity.Role;
import com.settlement.manager.domain.user.entity.User;
import com.settlement.manager.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SettlementConcurrencyTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeeRateRepository feeRateRepository;

    @Test
    @DisplayName("동시 확정 10스레드 -> 1번만 성공, 나머지는 예외")
    void concurrent_confirm_only_one_succeeds() throws InterruptedException {

        User creator = userRepository.save(User.builder()
                .email("concurrency@test.com")
                .password("password")
                .role(Role.ROLE_CREATOR)
                .build());

        feeRateRepository.save(FeeRate.builder()
                .rate(new BigDecimal("0.20"))
                .changedBy(1L)
                .grade(null)
                .build());

        Settlement settlement = settlementRepository.save(Settlement.builder()
                .creatorId(creator.getId())
                .year(2025)
                .month(3)
                .totalSaleAmount(new BigDecimal("100000"))
                .totalRefundAmount(BigDecimal.ZERO)
                .netSaleAmount(new BigDecimal("100000"))
                .feeRate(new BigDecimal("0.20"))
                .feeAmount(new BigDecimal("20000"))
                .settlementAmount(new BigDecimal("80000"))
                .saleCount(1)
                .cancelCount(0)
                .build());

        // 10스레드 동시 확정 시도
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    settlementService.confirm(settlement.getId(), 1L);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            }));
        }

        latch.countDown(); // 동시 시작
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 1번만 성공, 나머지 9번 실패
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);

        // 최종 상태 CONFIRMED
        Settlement result = settlementRepository.findById(settlement.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
    }
}