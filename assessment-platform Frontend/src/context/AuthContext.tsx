import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { AuthUser, LoginRequest, RegisterRequest } from '../types';
import { authApi } from '../api/auth';

interface AuthContextValue {
  user: AuthUser | null;
  token: string | null;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Restore session from localStorage on mount (only for active test attempts)
  useEffect(() => {
    const path = window.location.pathname;
    const isActiveAttemptPage = path.includes('/student/attempts/') && !path.endsWith('/review');

    if (!isActiveAttemptPage) {
      // Reload on any regular page -> clear session and force login
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('auth_user');
      setToken(null);
      setUser(null);
    } else {
      // Reload while taking an assessment -> preserve session to continue test
      const storedToken = localStorage.getItem('jwt_token');
      const storedUser = localStorage.getItem('auth_user');
      if (storedToken && storedUser) {
        try {
          setToken(storedToken);
          setUser(JSON.parse(storedUser));
        } catch {
          localStorage.removeItem('jwt_token');
          localStorage.removeItem('auth_user');
        }
      }
    }
    setIsLoading(false);
  }, []);

  const persistSession = (token: string, user: AuthUser) => {
    localStorage.setItem('jwt_token', token);
    localStorage.setItem('auth_user', JSON.stringify(user));
    setToken(token);
    setUser(user);
  };

  const login = useCallback(async (data: LoginRequest) => {
    const res = await authApi.login(data);
    const { token, userId, username, email, role } = res.data;
    const authUser: AuthUser = { id: userId || 0, username, email, role: role as 'TEACHER' | 'STUDENT' };
    persistSession(token, authUser);
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    const res = await authApi.register(data);
    const { token, userId, username, email, role } = res.data;
    const authUser: AuthUser = { id: userId || 0, username, email, role: role as 'TEACHER' | 'STUDENT' };
    persistSession(token, authUser);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('auth_user');
    setToken(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
