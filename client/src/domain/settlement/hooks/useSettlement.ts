import { useState } from 'react';
import { getSettlementApi } from '../api/settlementApi';
import { type SettlementResponse } from '../types/settlement.types';
import { useAuthStore } from '../../auth/store/authStore';
import { parseJwt } from '../../../shared/utils/jwt';

export function useSettlement() {
    const { accessToken } = useAuthStore();
    const [settlement, setSettlement] = useState<SettlementResponse | null>(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const creatorId = accessToken ? Number(parseJwt(accessToken).sub) : 0;

    const fetchSettlement = async (yearMonth: string) => {
        if (!yearMonth || !creatorId) return;
        setLoading(true);
        setError('');
        try {
            const data = await getSettlementApi(creatorId, yearMonth);
            setSettlement(data);
        } catch (err: any) {
            setError(err.response?.data?.message ?? '조회에 실패했습니다');
        } finally {
            setLoading(false);
        }
    };

    return { settlement, error, loading, fetchSettlement };
}