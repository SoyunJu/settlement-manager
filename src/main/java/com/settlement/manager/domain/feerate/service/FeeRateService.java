package com.settlement.manager.domain.feerate.service;

import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import com.settlement.manager.domain.feerate.dto.FeeRateChangeRequest;
import com.settlement.manager.domain.feerate.dto.FeeRateResponse;
import com.settlement.manager.domain.feerate.entity.FeeRate;
import com.settlement.manager.domain.feerate.repository.FeeRateRepository;
import com.settlement.manager.infrastructure.audit.AuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeRateService {

    private final FeeRateRepository feeRateRepository;
    private final FeeRateResolver feeRateResolver;
    private final AuditLogger auditLogger;

    // 현재 기본 수수료율 조회
    @Transactional(readOnly = true)
    public FeeRateResponse getCurrent() {
        return feeRateRepository.findTopByGradeIsNullOrderByCreatedAtDesc()
                .map(FeeRateResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.FEE_RATE_NOT_FOUND));
    }

    // 수수료율 변경
    @Transactional
    public FeeRateResponse change(FeeRateChangeRequest request, Long currentUserId) {
        FeeRate feeRate = FeeRate.builder()
                .rate(request.rate())
                .grade(request.grade())
                .changedBy(currentUserId)
                .build();
        feeRateRepository.save(feeRate);
        auditLogger.log("FEE_RATE_CHANGE", currentUserId,
                "rate=" + request.rate() + " grade=" + request.grade());
        log.info("수수료율 변경. rate={} grade={} changedBy={}", request.rate(), request.grade(), currentUserId);
        return FeeRateResponse.from(feeRate);
    }

    // 수수료율 history 조회
    @Transactional(readOnly = true)
    public List<FeeRateResponse> getHistory() {
        return feeRateRepository.findAll().stream()
                .map(FeeRateResponse::from)
                .toList();
    }
}