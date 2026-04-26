package com.settlement.manager.domain.sale.service;

import com.settlement.manager.common.exception.BusinessException;
import com.settlement.manager.common.exception.ErrorCode;
import com.settlement.manager.domain.sale.dto.CancelRecordCreateRequest;
import com.settlement.manager.domain.sale.entity.SaleRecord;
import com.settlement.manager.domain.sale.repository.CancelRecordRepository;
import com.settlement.manager.domain.sale.repository.SaleRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class SaleRecordServiceTest {

    @InjectMocks
    private SaleRecordService saleRecordService;

    @Mock
    private SaleRecordRepository saleRecordRepository;

    @Mock
    private CancelRecordRepository cancelRecordRepository;

    @Test
    @DisplayName("IDOR 방지 -> 타인의 판매 내역 조회 시 FORBIDDEN")
    void idor_creator_cannot_access_others_list() {
        Long creatorId = 1L;
        Long currentUserId = 2L;

        assertThatThrownBy(() -> saleRecordService.getList(creatorId, null, currentUserId, false))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("ADMIN은 전체 판매 내역 조회 가능")
    void admin_can_access_others_list() {
        Long creatorId = 1L;
        Long currentUserId = 99L;
        given(saleRecordRepository.findByCursor(anyLong(), anyLong(), anyInt()))
                .willReturn(List.of());

        assertThatNoException().isThrownBy(
                () -> saleRecordService.getList(creatorId, null, currentUserId, true));
    }

    @Test
    @DisplayName("취소 IDOR 방지 -> 타인의 판매 내역 취소시 FORBIDDEN")
    void idor_cancel_others_sale_forbidden() {
        SaleRecord original = SaleRecord.builder()
                .creatorId(1L)
                .courseId(1L)
                .studentId(1L)
                .amount(new BigDecimal("100000"))
                .paidAt(Instant.now())
                .build();
        given(saleRecordRepository.findById(anyLong())).willReturn(Optional.of(original));

        CancelRecordCreateRequest request = new CancelRecordCreateRequest(
                1L, new BigDecimal("50000"), Instant.now());

        assertThatThrownBy(() -> saleRecordService.createCancel(request, 2L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("환불액 > 결제액 -> 예외 처리")
    void refund_exceeds_original_amount() {
        SaleRecord original = SaleRecord.builder()
                .creatorId(1L)
                .courseId(1L)
                .studentId(1L)
                .amount(new BigDecimal("100000"))
                .paidAt(Instant.now())
                .build();
        given(saleRecordRepository.findById(anyLong())).willReturn(Optional.of(original));

        CancelRecordCreateRequest request = new CancelRecordCreateRequest(
                1L, new BigDecimal("100001"), Instant.now());

        assertThatThrownBy(() -> saleRecordService.createCancel(request, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFUND_EXCEEDS_ORIGINAL);
    }

    @Test
    @DisplayName("환불액==결제액 -> 허용 (전액 환불)")
    void full_refund_is_allowed() {
        SaleRecord original = SaleRecord.builder()
                .creatorId(1L)
                .courseId(1L)
                .studentId(1L)
                .amount(new BigDecimal("100000"))
                .paidAt(Instant.now())
                .build();
        given(saleRecordRepository.findById(anyLong())).willReturn(Optional.of(original));
        given(cancelRecordRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        CancelRecordCreateRequest request = new CancelRecordCreateRequest(
                1L, new BigDecimal("100000"), Instant.now());

        assertThatNoException().isThrownBy(() -> saleRecordService.createCancel(request, 1L));
    }
}