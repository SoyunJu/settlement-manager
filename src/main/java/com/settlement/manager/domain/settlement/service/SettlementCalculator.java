package com.settlement.manager.domain.settlement.service;

import com.settlement.manager.domain.feerate.entity.CreatorGrade;
import com.settlement.manager.domain.feerate.service.FeeRateResolver;
import com.settlement.manager.domain.sale.entity.CancelRecord;
import com.settlement.manager.domain.sale.entity.SaleRecord;
import com.settlement.manager.domain.sale.repository.CancelRecordRepository;
import com.settlement.manager.domain.sale.repository.SaleRecordRepository;
import com.settlement.manager.domain.settlement.entity.Settlement;
import com.settlement.manager.domain.settlement.value.SettlementPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementCalculator {

    private final SaleRecordRepository saleRecordRepository;
    private final CancelRecordRepository cancelRecordRepository;
    private final FeeRateResolver feeRateResolver;

    public Settlement calculate(Long creatorId, CreatorGrade grade, SettlementPeriod period) {
        // 판매 내역 조회 (KST 월 경계 기준)
        List<SaleRecord> sales = saleRecordRepository.findByCreatorIdAndPaidAtBetween(
                creatorId, period.startInstant(), period.endInstant());

        // 취소 내역 조회
        List<CancelRecord> cancels = cancelRecordRepository.findByCreatorIdAndPaidAtBetween(
                creatorId, period.startInstant(), period.endInstant());

        BigDecimal totalSale = sales.stream()
                .map(SaleRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefund = cancels.stream()
                .map(CancelRecord::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSale = totalSale.subtract(totalRefund);

        // 정산 시점 기준 수수료율 조회
        BigDecimal feeRate = feeRateResolver.resolve(grade, period.endInstant());
        BigDecimal feeAmount = netSale.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal settlementAmount = netSale.subtract(feeAmount);

        log.debug("정산 계산. creatorId={} period={} netSale={} feeRate={} settlementAmount={}",
                creatorId, period.yearMonth(), netSale, feeRate, settlementAmount);

        return Settlement.builder()
                .creatorId(creatorId)
                .year(period.year())
                .month(period.month())
                .totalSaleAmount(totalSale)
                .totalRefundAmount(totalRefund)
                .netSaleAmount(netSale)
                .feeRate(feeRate)
                .feeAmount(feeAmount)
                .settlementAmount(settlementAmount)
                .saleCount(sales.size())
                .cancelCount(cancels.size())
                .build();
    }
}