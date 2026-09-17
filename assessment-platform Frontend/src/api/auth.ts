import api from './axios';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
} from '../types';

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<AuthResponse>('/api/auth/login', data),

  register: (data: RegisterRequest) =>
    api.post<AuthResponse>('/api/auth/register', data),
};
