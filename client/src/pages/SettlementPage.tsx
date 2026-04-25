import { useState } from 'react';
import { useSettlement } from '../domain/settlement/hooks/useSettlement';
import { useLogout } from '../domain/auth/hooks/useLogin';
import SettlementCard from '../domain/settlement/components/SettlementCard';
import Button from '../shared/components/Button';
import styles from './SettlementPage.module.css';

export default function SettlementPage() {
    const [yearMonth, setYearMonth] = useState('');
    const { settlement, error, loading, fetchSettlement } = useSettlement();
    const { logout } = useLogout();

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h2>내 정산 조회</h2>
                <Button variant="outline" onClick={logout}>로그아웃</Button>
            </div>
            <div className={styles.searchRow}>
                <input
                    className={styles.input}
                    type="month"
                    value={yearMonth}
                    onChange={(e) => setYearMonth(e.target.value)}
                />
                <Button onClick={() => fetchSettlement(yearMonth)} disabled={loading}>
                    {loading ? '조회 중...' : '조회'}
                </Button>
            </div>
            {error && <p className={styles.error}>{error}</p>}
            {settlement && <SettlementCard settlement={settlement} />}
        </div>
    );
}