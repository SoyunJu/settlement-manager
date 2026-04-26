import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from '../domain/auth/store/authStore';
import LoginPage from '../pages/LoginPage';
import SignupPage from '../pages/SignupPage';
import ForgotPasswordPage from '../pages/ForgotPasswordPage';
import DashboardPage from '../pages/DashboardPage';
import SettlementPage from '../pages/SettlementPage';
import type {JSX} from "react";

function PrivateRoute({ children, roles }: { children: JSX.Element; roles?: string[] }) {
    const { accessToken, role } = useAuthStore();
    if (!accessToken) return <Navigate to="/login" replace />;
    if (roles && role && !roles.includes(role)) return <Navigate to="/settlement" replace />;
    return children;
}

export default function AppRouter() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route
                    path="/dashboard"
                    element={
                        <PrivateRoute roles={['ROLE_OPERATOR', 'ROLE_ADMIN']}>
                            <DashboardPage />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/settlement"
                    element={
                        <PrivateRoute>
                            <SettlementPage />
                        </PrivateRoute>
                    }
                />
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
}