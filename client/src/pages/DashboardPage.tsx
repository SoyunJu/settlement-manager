import { useState } from 'react';
import { useDashboard } from '../domain/settlement/hooks/useDashboard';
import { useLogout } from '../domain/auth/hooks/useLogin';
import DashboardTable from '../domain/settlement/components/DashboardTable';
import Button from '../shared/components/Button';
import { formatAmount, formatYearMonth } from '../shared/utils/format';
import styles from './DashboardPage.module.css';

export default function DashboardPage() {
    const [yearMonth, setYearMonth] = useState('');
    const { dashboard, error, loading, fetchDashboard, confirm, pay } = useDashboard();
    const { logout } = useLogout();

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h2>운영자 대시보드</h2>
                <Button variant="outline" onClick={logout}>로그아웃</Button>
            </div>
            <div className={styles.searchRow}>
                <input
                    className={styles.input}
                    type="month"
                    value={yearMonth}
                    onChange={(e) => setYearMonth(e.target.value)}
                />
                <Button onClick={() => fetchDashboard(yearMonth)} disabled={loading}>
                    {loading ? '조회 중...' : '조회'}
                </Button>
            </div>
            {error && <p className={styles.error}>{error}</p>}

            {dashboard?.summary && (
                <div className={styles.summaryBox}>
                    <h3>
                        전체 요약 ({formatYearMonth(dashboard.summary.year, dashboard.summary.month)})
                    </h3>
                    <div className={styles.summaryGrid}>
                        {[
                            { label: '총 판매', value: formatAmount(dashboard.summary.totalSaleAmount) },
                            { label: '총 환불', value: formatAmount(dashboard.summary.totalRefundAmount) },
                            { label: '순 이익', value: formatAmount(dashboard.summary.netSaleAmount) },
                            { label: '수수료', value: formatAmount(dashboard.summary.feeAmount) },
                            { label: '정산 예정', value: formatAmount(dashboard.summary.settlementAmount) },
                            { label: '정산 건수', value: `${dashboard.summary.settlementCount}건` },
                        ].map(({ label, value }) => (
                            <div key={label} className={styles.summaryItem}>
                                <span className={styles.label}>{label}</span>
                                <strong>{value}</strong>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {dashboard && (
                <DashboardTable
                    creators={dashboard.creators}
                    onConfirm={confirm}
                    onPay={pay}
                />
            )}
        </div>
    );
}