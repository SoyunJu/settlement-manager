import { create } from 'zustand';
import { type UserRole } from '../types/auth.types';

interface AuthState {
    accessToken: string | null;
    role: UserRole | null;
    setTokens: (accessToken: string, refreshToken: string, role: UserRole) => void;
    clearTokens: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
    accessToken: localStorage.getItem('accessToken'),
    role: localStorage.getItem('role') as UserRole | null,

    setTokens: (accessToken, refreshToken, role) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('role', role);
        set({ accessToken, role });
    },

    clearTokens: () => {
        localStorage.clear();
        set({ accessToken: null, role: null });
    },
}));