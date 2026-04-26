package com.settlement.manager.domain.settlement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.settlement.manager.domain.settlement.dto.CreatorStatRow;
import com.settlement.manager.domain.settlement.dto.DashboardResponse;
import com.settlement.manager.domain.settlement.dto.MonthlyStatRow;
import com.settlement.manager.domain.settlement.repository.DashboardQueryRepository;
import com.settlement.manager.domain.settlement.value.SettlementPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardQueryRepository dashboardQueryRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofHours(25);
    private static final String LOCK_KEY = "lock:dashboard:refresh";
    private static final String CACHE_KEY_PREFIX = "dashboard:";


    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String yearMonth) {
        SettlementPeriod period = SettlementPeriod.of(yearMonth);
        String cacheKey = CACHE_KEY_PREFIX + yearMonth;

        // 캐시 조회
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                log.debug("정산 캐시 히트. key={}", cacheKey);
                return objectMapper.readValue(cached, DashboardResponse.class);
            } catch (Exception e) {
                log.warn("정산 캐시 역직렬화 실패. DB 조회. key={}", cacheKey);
            }
        }

        return fetchAndCache(period, cacheKey);
    }


    // DB 조회 -> 캐시 저장 (TTL 25)
    private DashboardResponse fetchAndCache(SettlementPeriod period, String cacheKey) {
        List<MonthlyStatRow> monthly = dashboardQueryRepository
                .findMonthlyStats(period.year(), period.month());
        List<CreatorStatRow> creators = dashboardQueryRepository
                .findCreatorStats(period.year(), period.month());

        MonthlyStatRow summary = monthly.isEmpty() ? null : monthly.get(0);
        DashboardResponse response = new DashboardResponse(summary, creators);

        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL);
            log.info("정산 캐시 저장. key={} ttl={}h", cacheKey, CACHE_TTL.toHours());
        } catch (Exception e) {
            log.warn("정산 캐시 저장 실패. key={}", cacheKey);
        }
        return response;
    }

    // 캐시 강제 갱신 (분산 락)
    @Transactional(readOnly = true)
    public void refreshCache(String yearMonth) {
        RLock lock = redissonClient.getLock(LOCK_KEY + ":" + yearMonth);
        boolean acquired = false;
        try {
            // 락 획득 시도
            acquired = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!acquired) {
                log.info("정산 캐시 갱신 락 실패. 다른 인스턴스 실행 중. yearMonth={}", yearMonth);
                return;
            }
            SettlementPeriod period = SettlementPeriod.of(yearMonth);
            String cacheKey = CACHE_KEY_PREFIX + yearMonth;

            // 기존 캐시 삭제 후 재조회
            redisTemplate.delete(cacheKey);
            fetchAndCache(period, cacheKey);
            log.info("정산 캐시 갱신 완료. yearMonth={}", yearMonth);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("정산 캐시 갱신 인터럽트. yearMonth={}", yearMonth);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}