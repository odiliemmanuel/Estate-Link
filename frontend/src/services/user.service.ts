import { apiClient } from '../lib/apiClient'
import { SERVICE_ROUTES } from '../config/api'
import type { User, UserStatus } from '../types/user'

const base = SERVICE_ROUTES.user

// user-service: /api/v1/users
export const userService = {
  getById(id: string): Promise<User> {
    return apiClient.get<User>(`${base.users}/${id}`).then((r) => r.data)
  },

  update(
    id: string,
    payload: { name: string; email: string; password?: string },
  ): Promise<User> {
    return apiClient.put<User>(`${base.users}/${id}`, payload).then((r) => r.data)
  },

  // Admin only.
  getAllAgents(): Promise<User[]> {
    return apiClient.get<User[]>(`${base.users}/agents`).then((r) => r.data)
  },

  // Admin only — agents that registered but never verified their email.
  getPendingAgents(): Promise<User[]> {
    return apiClient.get<User[]>(`${base.users}/agents/pending`).then((r) => r.data)
  },

  getByStatus(status: UserStatus): Promise<User[]> {
    return apiClient.get<User[]>(`${base.users}/status/${status}`).then((r) => r.data)
  },

  updateStatus(id: string, status: UserStatus): Promise<User> {
    return apiClient
      .put<User>(`${base.users}/${id}/status`, null, { params: { status } })
      .then((r) => r.data)
  },
}
