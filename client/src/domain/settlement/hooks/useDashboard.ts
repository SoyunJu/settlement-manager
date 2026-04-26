import { useState } from 'react';
import { getDashboardApi, confirmSettlementApi, paySettlementApi } from '../api/settlementApi';
import { type DashboardResponse } from '../types/settlement.types';

export function useDashboard() {
    const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const fetchDashboard = async (yearMonth: string) => {
        if (!yearMonth) return;
        setLoading(true);
        setError('');
        try {
            const data = await getDashboardApi(yearMonth);
            setDashboard(data);
        } catch (err: any) {
            setError(err.response?.data?.message ?? '조회에 실패했습니다');
        } finally {
            setLoading(false);
        }
    };

    const confirm = async (settlementId: number) => {
        await confirmSettlementApi(settlementId);
        // 상태 갱신
    };

    const pay = async (settlementId: number) => {
        await paySettlementApi(settlementId);
    };

    return { dashboard, error, loading, fetchDashboard, confirm, pay };
}