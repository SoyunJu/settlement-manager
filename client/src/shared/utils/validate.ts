export const validateEmail = (email: string): string => {
    if (!email) return '이메일을 입력해주세요';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return '올바른 이메일 형식이 아닙니다';
    return '';
};

export const validatePassword = (password: string): string => {
    if (!password) return '비밀번호를 입력해주세요';
    if (password.length < 8) return '비밀번호는 8자 이상이어야 합니다';
    return '';
};

export const validatePasswordConfirm = (password: string, confirm: string): string => {
    if (!confirm) return '비밀번호 확인을 입력해주세요';
    if (password !== confirm) return '비밀번호가 일치하지 않습니다';
    return '';
};