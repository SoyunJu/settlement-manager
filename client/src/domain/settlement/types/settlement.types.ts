export type SettlementStatus = 'PENDING' | 'CONFIRMED' | 'PAID';

export interface SettlementResponse {
    id: number;
    creatorId: number;
    year: number;
    month: number;
    totalSaleAmount: number;
    totalRefundAmount: number;
    netSaleAmount: number;
    feeRate: number;
    feeAmount: number;
    settlementAmount: number;
    saleCount: number;
    cancelCount: number;
    status: SettlementStatus;
}

export interface DashboardSummary {
    year: number;
    month: number;
    totalSaleAmount: number;
    totalRefundAmount: number;
    netSaleAmount: number;
    feeAmount: number;
    settlementAmount: number;
    settlementCount: number;
}

export interface CreatorStat {
    creatorId: number;
    totalSaleAmount: number;
    netSaleAmount: number;
    settlementAmount: number;
    status: SettlementStatus;
}

export interface DashboardResponse {
    summary: DashboardSummary | null;
    creators: CreatorStat[];
}