package com.settlement.manager.domain.sale.service;

import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import com.settlement.manager.domain.sale.dto.*;
import com.settlement.manager.domain.sale.entity.CancelRecord;
import com.settlement.manager.domain.sale.entity.SaleRecord;
import com.settlement.manager.domain.sale.repository.CancelRecordRepository;
import com.settlement.manager.domain.sale.repository.SaleRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleRecordService {

    private final SaleRecordRepository saleRecordRepository;
    private final CancelRecordRepository cancelRecordRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;

    // 판매 내역 등록
    @Transactional
    public SaleRecordResponse create(SaleRecordCreateRequest request) {
        SaleRecord record = SaleRecord.builder()
                .creatorId(request.creatorId())
                .courseId(request.courseId())
                .studentId(request.studentId())
                .amount(request.amount())
                .paidAt(request.paidAt())
                .build();
        saleRecordRepository.save(record);
        log.info("판매 내역 등록. creatorId={} amount={}", record.getCreatorId(), record.getAmount());
        return SaleRecordResponse.from(record);
    }

    // 취소 내역 등록
    @Transactional
    public CancelRecordResponse createCancel(CancelRecordCreateRequest request, Long currentUserId) {
        SaleRecord original = saleRecordRepository.findById(request.originalSaleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SALE_NOT_FOUND));

        // IDOR 방지
        if (!original.getCreatorId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 환불 금액 검증
        if (request.refundAmount().compareTo(original.getAmount()) > 0) {
            throw new BusinessException(ErrorCode.REFUND_EXCEEDS_ORIGINAL);
        }

        CancelRecord cancel = CancelRecord.builder()
                .creatorId(original.getCreatorId())
                .originalSaleId(request.originalSaleId())
                .refundAmount(request.refundAmount())
                .paidAt(request.paidAt())
                .build();
        cancelRecordRepository.save(cancel);
        log.info("취소 내역 등록. originalSaleId={} refundAmount={}", request.originalSaleId(), request.refundAmount());
        return CancelRecordResponse.from(cancel);
    }


    // 판매 내역 목록 조회
    @Transactional(readOnly = true)
    public SaleRecordPageResponse getList(Long creatorId, Long cursor, Long currentUserId, boolean isAdmin) {
        // IDOR 방지
        if (!isAdmin && !creatorId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        long effectiveCursor = (cursor != null) ? cursor : 0L;
        List<SaleRecord> records = saleRecordRepository.findByCursor(creatorId, effectiveCursor, DEFAULT_PAGE_SIZE + 1);

        boolean hasNext = records.size() > DEFAULT_PAGE_SIZE;
        List<SaleRecord> content = hasNext ? records.subList(0, DEFAULT_PAGE_SIZE) : records;
        Long nextCursor = hasNext ? content.get(content.size() - 1).getId() : null;

        return new SaleRecordPageResponse(
                content.stream().map(SaleRecordResponse::from).toList(),
                nextCursor,
                hasNext
        );
    }
}