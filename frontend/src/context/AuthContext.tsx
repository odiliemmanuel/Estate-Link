import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authService } from '../services/auth.service'
import { clearStoredSession, getStoredToken, getStoredUser, setStoredSession } from '../lib/storage'
import type { Role, User } from '../types/user'

interface AuthContextValue {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<User>
  logout: () => void
  setUser: (user: User) => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getStoredToken())
  const [user, setUserState] = useState<User | null>(() => getStoredUser())

  const login = useCallback(async (email: string, password: string) => {
    const response = await authService.login({ email, password })
    setStoredSession(response.token, response.user)
    setToken(response.token)
    setUserState(response.user)
    return response.user
  }, [])

  const logout = useCallback(() => {
    clearStoredSession()
    setToken(null)
    setUserState(null)
  }, [])

  const setUser = useCallback((next: User) => {
    setUserState(next)
    const storedToken = getStoredToken()
    if (storedToken) setStoredSession(storedToken, next)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      isAuthenticated: Boolean(token && user),
      login,
      logout,
      setUser,
    }),
    [user, token, login, logout, setUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within an AuthProvider')
  return context
}

export type { Role }
