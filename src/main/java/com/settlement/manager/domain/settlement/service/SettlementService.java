package com.settlement.manager.domain.settlement.service;

import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import com.settlement.manager.domain.settlement.dto.SettlementResponse;
import com.settlement.manager.domain.settlement.entity.Settlement;
import com.settlement.manager.domain.settlement.repository.SettlementRepository;
import com.settlement.manager.domain.settlement.value.SettlementPeriod;
import com.settlement.manager.domain.user.entity.User;
import com.settlement.manager.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.settlement.manager.infrastructure.audit.AuditLogger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementCalculator settlementCalculator;
    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    // 월별 정산 조회
    @Transactional
    public SettlementResponse getOrCreate(Long creatorId, String yearMonth, Long currentUserId, boolean isAdmin) {
        // IDOR 방지
        if (!isAdmin && !creatorId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        SettlementPeriod period = SettlementPeriod.of(yearMonth);

        // 미래 월 조회 방지
        if (period.isFuture()) {
            throw new BusinessException(ErrorCode.FUTURE_PERIOD_NOT_ALLOWED);
        }

        // 정산 존재하면 반환
        return settlementRepository
                .findByCreatorIdAndYearAndMonth(creatorId, period.year(), period.month())
                .map(SettlementResponse::from)
                .orElseGet(() -> {
                    User user = userRepository.findById(creatorId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                    Settlement settlement = settlementCalculator.calculate(creatorId, user.getGrade(), period);
                    settlementRepository.save(settlement);
                    log.info("정산 생성. creatorId={} period={} settlementAmount={}",
                            creatorId, yearMonth, settlement.getSettlementAmount());
                    return SettlementResponse.from(settlement);
                });
    }

    // 정산 확정 (PENDING -> CONFIRMED). 비관적 락으로 동시 확정 방지.
    @Transactional
    public SettlementResponse confirm(Long settlementId, Long currentUserId) {
        Settlement settlement = settlementRepository.findByIdWithLock(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        settlement.confirm();
        auditLogger.log("SETTLEMENT_CONFIRM", currentUserId,
                "settlementId=" + settlementId + " creatorId=" + settlement.getCreatorId()
                        + " month=" + settlement.getYear() + "-" + settlement.getMonth());
        log.info("정산 확정. settlementId={} creatorId={}", settlementId, settlement.getCreatorId());
        return SettlementResponse.from(settlement);
    }

    // 정산 지급 완료 -> Mock 알림 로그. TODO: 실제 메신저나 이메일 연동
    @Transactional
    public SettlementResponse pay(Long settlementId, Long currentUserId) {
        Settlement settlement = settlementRepository.findByIdWithLock(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        settlement.pay();
        auditLogger.log("SETTLEMENT_PAID", currentUserId,
                "settlementId=" + settlementId + " creatorId=" + settlement.getCreatorId()
                        + " amount=" + settlement.getSettlementAmount());

        // PAID 전환 시 -> Mock 알림 로그 TODO: 실제 메신저나 이메일 연동
        log.info("[NOTIFY] creatorId={} month={}-{} amount={}",
                settlement.getCreatorId(), settlement.getYear(), settlement.getMonth(),
                settlement.getSettlementAmount());
        return SettlementResponse.from(settlement);
    }
}