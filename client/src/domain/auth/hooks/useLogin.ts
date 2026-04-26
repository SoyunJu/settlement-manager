import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginApi, logoutApi, signupApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';
import { parseJwt } from '../../../shared/utils/jwt';
import type {SignupRequest, UserRole} from '../types/auth.types';

export function useLogin() {
    const navigate = useNavigate();
    const { setTokens } = useAuthStore();
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const login = async (email: string, password: string) => {
        setError('');
        setLoading(true);
        try {
            const { accessToken, refreshToken } = await loginApi({ email, password });
            const payload = parseJwt(accessToken);
            const role = payload.role as UserRole;
            setTokens(accessToken, refreshToken, role);
            if (role === 'ROLE_OPERATOR' || role === 'ROLE_ADMIN') {
                navigate('/dashboard');
            } else {
                navigate('/settlement');
            }
        } catch (err: any) {
            setError(err.response?.data?.message ?? '로그인에 실패했습니다');
        } finally {
            setLoading(false);
        }
    };

    return { login, error, loading };
}

export function useSignup() {
    const navigate = useNavigate();
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    const signup = async (req: SignupRequest) => {
        setError('');
        setLoading(true);
        try {
            await signupApi(req);
            setSuccess(true);
            setTimeout(() => navigate('/login'), 1500);
        } catch (err: any) {
            setError(err.response?.data?.message ?? '회원가입에 실패했습니다');
        } finally {
            setLoading(false);
        }
    };

    return { signup, error, loading, success };
}

export function useLogout() {
    const navigate = useNavigate();
    const { clearTokens } = useAuthStore();

    const logout = async () => {
        try { await logoutApi(); } catch { /* 무시 */ }
        clearTokens();
        navigate('/login');
    };

    return { logout };
}