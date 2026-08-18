// Mirrors user-service enums: com.estatelink.user.data.model
export type Role = 'OWNER' | 'AGENT' | 'APPLICANT' | 'ADMIN'
export type UserStatus = 'UNVERIFIED' | 'ACTIVE' | 'SUSPENDED'

// Matches dtos/responses/UserResponse
export interface User {
  id: string
  name: string
  email: string
  role: Role
  status: UserStatus
  createdAt: string
}

// Matches dtos/requests/RegisterRequest
export interface RegisterRequest {
  name: string
  email: string
  password: string
  role: Role
}

// Matches dtos/requests/LoginRequest
export interface LoginRequest {
  email: string
  password: string
}

// Matches dtos/responses/AuthResponse
export interface AuthResponse {
  token: string
  type: 'Bearer'
  user: User
}
