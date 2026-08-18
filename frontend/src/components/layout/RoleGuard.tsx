import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import type { Role } from '../../types/user'

// Restricts a route to specific roles. Unauthenticated users go to /login;
// authenticated users with the wrong role go to the dashboard.
export function RoleGuard({
  roles,
  children,
}: {
  roles: Role[]
  children: ReactNode
}) {
  const { isAuthenticated, user } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  if (!user || !roles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />
  }
  return <>{children}</>
}
