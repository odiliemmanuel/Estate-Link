import { apiClient } from '../lib/apiClient'
import { SERVICE_ROUTES } from '../config/api'
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/user'

const base = SERVICE_ROUTES.user

// user-service: /api/v1/auth
export const authService = {
  register(payload: RegisterRequest): Promise<User> {
    return apiClient.post<User>(`${base.auth}/register`, payload).then((r) => r.data)
  },

  login(payload: LoginRequest): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>(`${base.auth}/login`, payload).then((r) => r.data)
  },

  verifyEmail(token: string): Promise<string> {
    return apiClient
      .get<string>(`${base.auth}/verify`, { params: { token } })
      .then((r) => r.data)
  },
}
