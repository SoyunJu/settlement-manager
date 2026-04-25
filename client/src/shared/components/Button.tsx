import styles from './Button.module.css';

interface Props extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'success' | 'outline';
}

export default function Button({ variant = 'primary', className, children, ...rest }: Props) {
    return (
        <button className={`${styles.button} ${styles[variant]} ${className ?? ''}`} {...rest}>
            {children}
        </button>
    );
}