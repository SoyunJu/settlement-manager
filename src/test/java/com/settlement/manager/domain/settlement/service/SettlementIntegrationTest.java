package com.settlement.manager.domain.settlement.service;

import com.settlement.manager.domain.feerate.entity.FeeRate;
import com.settlement.manager.domain.feerate.repository.FeeRateRepository;
import com.settlement.manager.domain.sale.entity.SaleRecord;
import com.settlement.manager.domain.sale.repository.SaleRecordRepository;
import com.settlement.manager.domain.settlement.dto.SettlementResponse;
import com.settlement.manager.domain.settlement.entity.SettlementStatus;
import com.settlement.manager.domain.user.entity.Role;
import com.settlement.manager.domain.user.entity.User;
import com.settlement.manager.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SettlementIntegrationTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SaleRecordRepository saleRecordRepository;

    @Autowired
    private FeeRateRepository feeRateRepository;

    @Autowired
    private UserRepository userRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("통합 시나리오1 - 1월 판매 2건 -> 1월 정산 정상 생성")
    void integration_january_settlement_created() {
        User creator = userRepository.save(User.builder()
                .email("creator@test.com")
                .password("password")
                .role(Role.ROLE_CREATOR)
                .build());

        feeRateRepository.save(FeeRate.builder()
                .rate(new BigDecimal("0.20"))
                .changedBy(1L)
                .grade(null)
                .build());

        // 1월 15일 KST 판매
        Instant jan15Kst = LocalDateTime.of(2025, 1, 15, 10, 0)
                .atZone(KST).toInstant();
        saleRecordRepository.save(SaleRecord.builder()
                .creatorId(creator.getId())
                .courseId(1L)
                .studentId(1L)
                .amount(new BigDecimal("100000"))
                .paidAt(jan15Kst)
                .build());

        // 1월 31일 23:30 KST (경계값)
        Instant jan31_2330_kst = LocalDateTime.of(2025, 1, 31, 23, 30)
                .atZone(KST).toInstant();
        saleRecordRepository.save(SaleRecord.builder()
                .creatorId(creator.getId())
                .courseId(2L)
                .studentId(2L)
                .amount(new BigDecimal("50000"))
                .paidAt(jan31_2330_kst)
                .build());

        SettlementResponse response = settlementService.getOrCreate(
                creator.getId(), "2025-01", creator.getId(), false);

        assertThat(response.totalSaleAmount()).isEqualByComparingTo("150000");
        assertThat(response.feeAmount()).isEqualByComparingTo("30000.00");
        assertThat(response.settlementAmount()).isEqualByComparingTo("120000.00");
        assertThat(response.saleCount()).isEqualTo(2);
        assertThat(response.status()).isEqualTo(SettlementStatus.PENDING);
    }

    @Test
    @DisplayName("통합 시나리오2 - 동일 기간 두 번 조회 -> 중복 생성X 동일 결과")
    void integration_duplicate_settlement_not_created() {
        User creator = userRepository.save(User.builder()
                .email("creator2@test.com")
                .password("password")
                .role(Role.ROLE_CREATOR)
                .build());

        feeRateRepository.save(FeeRate.builder()
                .rate(new BigDecimal("0.20"))
                .changedBy(1L)
                .grade(null)
                .build());

        SettlementResponse first = settlementService.getOrCreate(
                creator.getId(), "2025-01", creator.getId(), false);
        SettlementResponse second = settlementService.getOrCreate(
                creator.getId(), "2025-01", creator.getId(), false);

        assertThat(first.id()).isEqualTo(second.id());
    }
}