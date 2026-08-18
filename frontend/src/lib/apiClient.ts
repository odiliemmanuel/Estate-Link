import axios from 'axios'
import { API_BASE } from '../config/api'
import { clearStoredSession, getStoredToken } from './storage'
import { toApiError } from './error'

export const apiClient = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
})

// Attach the JWT to every request. When the api-gateway is introduced it will
// validate this token once; until then each microservice validates it itself.
apiClient.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Normalise backend error bodies into ApiError and clear an expired session.
apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      const apiError = toApiError(error)
      if (apiError.status === 401) {
        clearStoredSession()
      }
      return Promise.reject(apiError)
    }
    return Promise.reject(error)
  },
)
