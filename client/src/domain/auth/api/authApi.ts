import api from '../../../shared/api/axiosInstance';
import type {LoginRequest, SignupRequest, TokenResponse} from '../types/auth.types';

export const loginApi = async (req: LoginRequest): Promise<TokenResponse> => {
    const { data } = await api.post('/api/v1/auth/login', req);
    return data.data;
};

export const signupApi = async (req: SignupRequest): Promise<void> => {
    await api.post('/api/v1/auth/signup', req);
};

export const logoutApi = async (): Promise<void> => {
    await api.post('/api/v1/auth/logout');
};