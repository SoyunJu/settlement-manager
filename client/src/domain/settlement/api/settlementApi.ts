import api from '../../../shared/api/axiosInstance';
import { type SettlementResponse, type DashboardResponse } from '../types/settlement.types';

export const getSettlementApi = async (
    creatorId: number,
    yearMonth: string
): Promise<SettlementResponse> => {
    const { data } = await api.get('/api/v1/settlements', { params: { creatorId, yearMonth } });
    return data.data;
};

export const getDashboardApi = async (yearMonth: string): Promise<DashboardResponse> => {
    const { data } = await api.get('/api/v1/dashboard', { params: { yearMonth } });
    return data.data;
};

export const confirmSettlementApi = async (settlementId: number): Promise<SettlementResponse> => {
    const { data } = await api.post(`/api/v1/settlements/${settlementId}/confirm`);
    return data.data;
};

export const paySettlementApi = async (settlementId: number): Promise<SettlementResponse> => {
    const { data } = await api.post(`/api/v1/settlements/${settlementId}/pay`);
    return data.data;
};