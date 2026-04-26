import { useState } from 'react';
import { type CreatorStat } from '../types/settlement.types';
import Badge from '../../../shared/components/Badge';
import Button from '../../../shared/components/Button';
import { formatAmount } from '../../../shared/utils/format';
import styles from './DashboardTable.module.css';

interface Props {
    creators: CreatorStat[];
    onConfirm: (id: number) => void;
    onPay: (id: number) => void;
}

export default function DashboardTable({ creators, onConfirm, onPay }: Props) {
    const [expandedRows, setExpandedRows] = useState<Set<number>>(new Set());

    const toggle = (id: number) => {
        setExpandedRows((prev) => {
            const next = new Set(prev);
            next.has(id) ? next.delete(id) : next.add(id);
            return next;
        });
    };

    return (
        <table className={styles.table}>
            <thead>
            <tr>
                <th></th>
                <th>크리에이터 ID</th>
                <th>순 판매</th>
                <th>정산 예정</th>
                <th>상태</th>
            </tr>
            </thead>
            <tbody>
            {creators.map((c) => (
                <>
                    <tr key={c.creatorId}>
                        <td>
                            <button className={styles.toggleBtn} onClick={() => toggle(c.creatorId)}>
                                {expandedRows.has(c.creatorId) ? '▲' : '▼'}
                            </button>
                        </td>
                        <td>{c.creatorId}</td>
                        <td>{formatAmount(c.netSaleAmount)}</td>
                        <td>{formatAmount(c.settlementAmount)}</td>
                        <td><Badge status={c.status} /></td>
                    </tr>
                    {expandedRows.has(c.creatorId) && (
                        <tr key={`detail-${c.creatorId}`} className={styles.detailRow}>
                            <td colSpan={5}>
                                <div className={styles.detailBox}>
                                    <div className={styles.detailGrid}>
                                        <div className={styles.detailItem}>
                                            <span className={styles.label}>총 판매</span>
                                            <strong>{formatAmount(c.totalSaleAmount)}</strong>
                                        </div>
                                        <div className={styles.detailItem}>
                                            <span className={styles.label}>순 판매</span>
                                            <strong>{formatAmount(c.netSaleAmount)}</strong>
                                        </div>
                                        <div className={styles.detailItem}>
                                            <span className={styles.label}>정산 예정</span>
                                            <strong>{formatAmount(c.settlementAmount)}</strong>
                                        </div>
                                    </div>
                                    <div className={styles.actionRow}>
                                        {c.status === 'PENDING' && (
                                            <Button onClick={() => onConfirm(c.creatorId)}>확정</Button>
                                        )}
                                        {c.status === 'CONFIRMED' && (
                                            <Button variant="success" onClick={() => onPay(c.creatorId)}>
                                                지급 완료
                                            </Button>
                                        )}
                                    </div>
                                </div>
                            </td>
                        </tr>
                    )}
                </>
            ))}
            </tbody>
        </table>
    );
}