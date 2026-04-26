import styles from './Badge.module.css';
import { statusLabel, statusColor } from '../utils/format';

interface Props {
    status: string;
}

export default function Badge({ status }: Props) {
    return (
        <span
            className={styles.badge}
            style={{ backgroundColor: statusColor[status] ?? '#eee' }}
        >
      {statusLabel[status] ?? status}
    </span>
    );
}