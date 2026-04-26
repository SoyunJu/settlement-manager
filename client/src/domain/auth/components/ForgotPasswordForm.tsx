import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { validateEmail } from '../../../shared/utils/validate';
import Button from '../../../shared/components/Button';
import styles from './ForgotPasswordForm.module.css';

export default function ForgotPasswordForm() {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [fieldError, setFieldError] = useState('');
    const [submitted, setSubmitted] = useState(false);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const error = validateEmail(email);
        if (error) { setFieldError(error); return; }
        // 관리자 문의 안내. TODO : 이메일 연동
        setSubmitted(true);
    };

    return (
        <div className={styles.container}>
            <form onSubmit={handleSubmit} className={styles.form} noValidate>
                <h2 className={styles.title}>비밀번호 찾기</h2>

                {!submitted ? (
                    <>
                        <p className={styles.description}>
                            가입 시 사용한 이메일을 입력하세요.
                        </p>

                        <div className={styles.inputWrapper}>
                            <input
                                className={`${styles.input} ${fieldError ? styles.inputError : ''}`}
                                type="email"
                                placeholder="이메일"
                                value={email}
                                onChange={(e) => { setEmail(e.target.value); setFieldError(''); }}
                            />
                            {fieldError && <span className={styles.fieldError}>{fieldError}</span>}
                        </div>

                        <p className={styles.notice}>
                            비밀번호 재설정 기능은 현재 관리자 문의를 통해 처리됩니다.
                            이메일 확인 후 운영자에게 전달됩니다.
                        </p>

                        <Button type="submit">확인</Button>
                    </>
                ) : (
                    <>
                        <p className={styles.description}>
                            <strong>{email}</strong>으로 요청이 접수되었습니다.<br />
                            운영자 확인 후 안내드리겠습니다.
                        </p>
                        <Button onClick={() => navigate('/login')}>로그인으로 돌아가기</Button>
                    </>
                )}

                {!submitted && (
                    <button type="button" className={styles.backLink} onClick={() => navigate('/login')}>
                        로그인으로 돌아가기
                    </button>
                )}
            </form>
        </div>
    );
}