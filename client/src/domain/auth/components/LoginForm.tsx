import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useLogin } from '../hooks/useLogin';
import { validateEmail, validatePassword } from '../../../shared/utils/validate';
import Button from '../../../shared/components/Button';
import styles from './LoginForm.module.css';

export default function LoginForm() {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [fieldErrors, setFieldErrors] = useState({ email: '', password: '' });
    const { login, error, loading } = useLogin();

    const validate = (): boolean => {
        const errors = {
            email: validateEmail(email),
            password: validatePassword(password),
        };
        setFieldErrors(errors);
        return !errors.email && !errors.password;
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!validate()) return;
        login(email, password);
    };

    return (
        <div className={styles.container}>
            <form onSubmit={handleSubmit} className={styles.form} noValidate>
                <h2 className={styles.title}>정산 관리 시스템</h2>

                <div className={styles.inputWrapper}>
                    <input
                        className={`${styles.input} ${fieldErrors.email ? styles.inputError : ''}`}
                        type="email"
                        placeholder="이메일"
                        value={email}
                        onChange={(e) => { setEmail(e.target.value); setFieldErrors(p => ({ ...p, email: '' })); }}
                    />
                    {fieldErrors.email && <span className={styles.fieldError}>{fieldErrors.email}</span>}
                </div>

                <div className={styles.inputWrapper}>
                    <input
                        className={`${styles.input} ${fieldErrors.password ? styles.inputError : ''}`}
                        type="password"
                        placeholder="비밀번호 (8자 이상)"
                        value={password}
                        onChange={(e) => { setPassword(e.target.value); setFieldErrors(p => ({ ...p, password: '' })); }}
                    />
                    {fieldErrors.password && <span className={styles.fieldError}>{fieldErrors.password}</span>}
                </div>

                {error && <p className={styles.error}>{error}</p>}

                <Button type="submit" disabled={loading}>
                    {loading ? '로그인 중...' : '로그인'}
                </Button>

                <div className={styles.links}>
                    <button type="button" className={styles.link} onClick={() => navigate('/signup')}>
                        회원가입
                    </button>
                    <button type="button" className={styles.link} onClick={() => navigate('/forgot-password')}>
                        비밀번호 찾기
                    </button>
                </div>
            </form>
        </div>
    );
}