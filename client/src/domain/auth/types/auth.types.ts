export interface LoginRequest {
    email: string;
    password: string;
}

export interface SignupRequest {
    email: string;
    password: string;
    role: UserRole;
}

export interface TokenResponse {
    accessToken: string;
    refreshToken: string;
}

export type UserRole = 'ROLE_CREATOR' | 'ROLE_OPERATOR' | 'ROLE_ADMIN';