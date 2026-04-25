import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSignup } from '../hooks/useLogin';
import { type UserRole } from '../types/auth.types';
import {
    validateEmail,
    validatePassword,
    validatePasswordConfirm,
} from '../../../shared/utils/validate';
import Button from '../../../shared/components/Button';
import styles from './SignupForm.module.css';

export default function SignupForm() {
    const navigate = useNavigate();
    const { signup, error, loading, success } = useSignup();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [passwordConfirm, setPasswordConfirm] = useState('');
    const [role, setRole] = useState<UserRole>('ROLE_CREATOR');
    const [fieldErrors, setFieldErrors] = useState({
        email: '', password: '', passwordConfirm: '',
    });

    const validate = (): boolean => {
        const errors = {
            email: validateEmail(email),
            password: validatePassword(password),
            passwordConfirm: validatePasswordConfirm(password, passwordConfirm),
        };
        setFieldErrors(errors);
        return !errors.email && !errors.password && !errors.passwordConfirm;
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!validate()) return;
        signup({ email, password, role });
    };

    return (
        <div className={styles.container}>
            <form onSubmit={handleSubmit} className={styles.form} noValidate>
                <h2 className={styles.title}>회원가입</h2>

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

                <div className={styles.inputWrapper}>
                    <input
                        className={`${styles.input} ${fieldErrors.passwordConfirm ? styles.inputError : ''}`}
                        type="password"
                        placeholder="비밀번호 확인"
                        value={passwordConfirm}
                        onChange={(e) => { setPasswordConfirm(e.target.value); setFieldErrors(p => ({ ...p, passwordConfirm: '' })); }}
                    />
                    {fieldErrors.passwordConfirm && (
                        <span className={styles.fieldError}>{fieldErrors.passwordConfirm}</span>
                    )}
                </div>

                <div className={styles.inputWrapper}>
                    <label className={styles.label}>역할</label>
                    <select
                        className={styles.select}
                        value={role}
                        onChange={(e) => setRole(e.target.value as UserRole)}
                    >
                        <option value="ROLE_CREATOR">크리에이터</option>
                        <option value="ROLE_OPERATOR">운영자</option>
                    </select>
                </div>

                {error && <p className={styles.error}>{error}</p>}
                {success && <p className={styles.success}>회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.</p>}

                <Button type="submit" disabled={loading || success}>
                    {loading ? '처리 중...' : '가입하기'}
                </Button>

                <button type="button" className={styles.backLink} onClick={() => navigate('/login')}>
                    로그인으로 돌아가기
                </button>
            </form>
        </div>
    );
}