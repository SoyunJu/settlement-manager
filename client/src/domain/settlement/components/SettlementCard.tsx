import { type SettlementResponse } from '../types/settlement.types';
import Badge from '../../../shared/components/Badge';
import { formatAmount, formatYearMonth } from '../../../shared/utils/format';
import styles from './SettlementCard.module.css';

interface Props {
    settlement: SettlementResponse;
}

export default function SettlementCard({ settlement }: Props) {
    return (
        <div className={styles.card}>
            <div className={styles.header}>
                <span>{formatYearMonth(settlement.year, settlement.month)} 정산</span>
                <Badge status={settlement.status} />
            </div>
            <div className={styles.grid}>
                <div className={styles.gridItem}>
                    <span className={styles.label}>총 판매금액</span>
                    <strong>{formatAmount(settlement.totalSaleAmount)}</strong>
                </div>
                <div className={styles.gridItem}>
                    <span className={styles.label}>총 환불금액</span>
                    <strong className={styles.amountNegative}>
                        -{formatAmount(settlement.totalRefundAmount)}
                    </strong>
                </div>
                <div className={styles.gridItem}>
                    <span className={styles.label}>순 이익</span>
                    <strong>{formatAmount(settlement.netSaleAmount)}</strong>
                </div>
                <div className={styles.gridItem}>
                    <span className={styles.label}>수수료율</span>
                    <strong>{(settlement.feeRate * 100).toFixed(1)}%</strong>
                </div>
                <div className={styles.gridItem}>
                    <span className={styles.label}>수수료</span>
                    <strong className={styles.amountNegative}>
                        -{formatAmount(settlement.feeAmount)}
                    </strong>
                </div>
                <div className={styles.gridItemHighlight}>
                    <span className={styles.label}>최종 정산 예정액</span>
                    <strong className={styles.amountHighlight}>
                        {formatAmount(settlement.settlementAmount)}
                    </strong>
                </div>
            </div>
            <div className={styles.footer}>
                판매 {settlement.saleCount}건 / 취소 {settlement.cancelCount}건
            </div>
        </div>
    );
}