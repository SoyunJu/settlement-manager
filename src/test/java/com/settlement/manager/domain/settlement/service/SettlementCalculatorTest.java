package com.settlement.manager.domain.settlement.service;

import com.settlement.manager.domain.feerate.service.FeeRateResolver;
import com.settlement.manager.domain.sale.entity.CancelRecord;
import com.settlement.manager.domain.sale.entity.SaleRecord;
import com.settlement.manager.domain.sale.repository.CancelRecordRepository;
import com.settlement.manager.domain.sale.repository.SaleRecordRepository;
import com.settlement.manager.domain.settlement.entity.Settlement;
import com.settlement.manager.domain.settlement.entity.SettlementStatus;
import com.settlement.manager.domain.settlement.value.SettlementPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SettlementCalculatorTest {

    @InjectMocks
    private SettlementCalculator settlementCalculator;

    @Mock
    private SaleRecordRepository saleRecordRepository;

    @Mock
    private CancelRecordRepository cancelRecordRepository;

    @Mock
    private FeeRateResolver feeRateResolver;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Long CREATOR_ID = 1L;
    private static final SettlementPeriod PERIOD = SettlementPeriod.of("2025-01");

    @Test
    @DisplayName("정상 계산 -> 판매 3건, 취소 1건, 수수료율 20%")
    void calculate_normal() {
        given(saleRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of(
                        mockSale(new BigDecimal("100000")),
                        mockSale(new BigDecimal("50000")),
                        mockSale(new BigDecimal("30000"))
                ));
        given(cancelRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of(mockCancel(new BigDecimal("20000"))));
        given(feeRateResolver.resolve(any(), any())).willReturn(new BigDecimal("0.20"));

        Settlement result = settlementCalculator.calculate(CREATOR_ID, null, PERIOD);

        assertThat(result.getTotalSaleAmount()).isEqualByComparingTo("180000");
        assertThat(result.getTotalRefundAmount()).isEqualByComparingTo("20000");
        assertThat(result.getNetSaleAmount()).isEqualByComparingTo("160000");
        assertThat(result.getFeeAmount()).isEqualByComparingTo("32000.00");
        assertThat(result.getSettlementAmount()).isEqualByComparingTo("128000.00");
        assertThat(result.getSaleCount()).isEqualTo(3);
        assertThat(result.getCancelCount()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(SettlementStatus.PENDING);
    }

    @Test
    @DisplayName("빈 월 예외 처리 -> 판매/취소 없으면 0원 정산")
    void calculate_empty_month_returns_zero() {
        given(saleRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of());
        given(cancelRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of());
        given(feeRateResolver.resolve(any(), any())).willReturn(new BigDecimal("0.20"));

        Settlement result = settlementCalculator.calculate(CREATOR_ID, null, PERIOD);

        assertThat(result.getTotalSaleAmount()).isEqualByComparingTo("0");
        assertThat(result.getSettlementAmount()).isEqualByComparingTo("0.00");
        assertThat(result.getSaleCount()).isEqualTo(0);
        assertThat(result.getCancelCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("부분 환불 -> 환불액 < 판매액")
    void calculate_partial_refund() {
        given(saleRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of(mockSale(new BigDecimal("100000"))));
        given(cancelRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of(mockCancel(new BigDecimal("30000"))));
        given(feeRateResolver.resolve(any(), any())).willReturn(new BigDecimal("0.20"));

        Settlement result = settlementCalculator.calculate(CREATOR_ID, null, PERIOD);

        assertThat(result.getNetSaleAmount()).isEqualByComparingTo("70000");
        assertThat(result.getFeeAmount()).isEqualByComparingTo("14000.00");
        assertThat(result.getSettlementAmount()).isEqualByComparingTo("56000.00");
    }

    @Test
    @DisplayName("2월 취소 -> 2월 정산에서 차감 (기간 필터 검증)")
    void february_cancel_deducted_in_february() {
        SettlementPeriod febPeriod = SettlementPeriod.of("2025-02");

        given(saleRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of(mockSale(new BigDecimal("100000"))));
        given(cancelRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of(mockCancel(new BigDecimal("50000"))));
        given(feeRateResolver.resolve(any(), any())).willReturn(new BigDecimal("0.10"));

        Settlement result = settlementCalculator.calculate(CREATOR_ID, null, febPeriod);

        assertThat(result.getMonth()).isEqualTo(2);
        assertThat(result.getNetSaleAmount()).isEqualByComparingTo("50000");
    }

    @Test
    @DisplayName("수수료율 소수점 반올림 검증")
    void fee_amount_rounded_half_up() {
        given(saleRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of(mockSale(new BigDecimal("10001"))));
        given(cancelRecordRepository.findByCreatorIdAndPaidAtBetween(eq(CREATOR_ID), any(), any()))
                .willReturn(List.of());
        given(feeRateResolver.resolve(any(), any())).willReturn(new BigDecimal("0.20"));

        Settlement result = settlementCalculator.calculate(CREATOR_ID, null, PERIOD);

        // 10001 * 0.20 = 2000.20 -> HALF_UP -> 2000.20
        assertThat(result.getFeeAmount()).isEqualByComparingTo("2000.20");
    }

    // --- 헬퍼 ---

    private SaleRecord mockSale(BigDecimal amount) {
        return SaleRecord.builder()
                .creatorId(CREATOR_ID)
                .courseId(1L)
                .studentId(1L)
                .amount(amount)
                .paidAt(Instant.now())
                .build();
    }

    private CancelRecord mockCancel(BigDecimal refundAmount) {
        return CancelRecord.builder()
                .creatorId(CREATOR_ID)
                .originalSaleId(1L)
                .refundAmount(refundAmount)
                .paidAt(Instant.now())
                .build();
    }
}