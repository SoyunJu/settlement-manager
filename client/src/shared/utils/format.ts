export const formatAmount = (amount: number): string =>
    (amount ?? 0).toLocaleString('ko-KR') + '원';

export const formatYearMonth = (year: number, month: number): string =>
    `${year}년 ${String(month).padStart(2, '0')}월`;

export const statusLabel: Record<string, string> = {
    PENDING: '정산 대기',
    CONFIRMED: '정산 확정',
    PAID: '지급 완료',
};

export const statusColor: Record<string, string> = {
    PENDING: '#fff3e0',
    CONFIRMED: '#e3f2fd',
    PAID: '#e8f5e9',
};