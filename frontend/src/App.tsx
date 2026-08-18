import { lazy, Suspense } from 'react'
import { Route, Routes } from 'react-router-dom'
import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress'
import Typography from '@mui/material/Typography'
import { AppLayout } from './components/layout/AppLayout'
import { ProtectedRoute } from './components/layout/ProtectedRoute'
import { RoleGuard } from './components/layout/RoleGuard'

// Route-level code splitting: each page chunk loads on demand.
const LandingPage = lazy(() => import('./pages/LandingPage').then((m) => ({ default: m.LandingPage })))
const BrowsePage = lazy(() => import('./pages/BrowsePage').then((m) => ({ default: m.BrowsePage })))
const LoginPage = lazy(() => import('./pages/auth/LoginPage').then((m) => ({ default: m.LoginPage })))
const RegisterPage = lazy(() => import('./pages/auth/RegisterPage').then((m) => ({ default: m.RegisterPage })))
const VerifyEmailPage = lazy(() =>
  import('./pages/auth/VerifyEmailPage').then((m) => ({ default: m.VerifyEmailPage })),
)
const VerifyPromptPage = lazy(() =>
  import('./pages/auth/VerifyEmailPage').then((m) => ({ default: m.VerifyPromptPage })),
)
const DashboardPage = lazy(() => import('./pages/DashboardPage').then((m) => ({ default: m.DashboardPage })))
const MyPropertiesPage = lazy(() => import('./pages/MyPropertiesPage').then((m) => ({ default: m.MyPropertiesPage })))
const CreatePropertyPage = lazy(() =>
  import('./pages/CreatePropertyPage').then((m) => ({ default: m.CreatePropertyPage })),
)
const PropertyDetailPage = lazy(() =>
  import('./pages/PropertyDetailPage').then((m) => ({ default: m.PropertyDetailPage })),
)
const CreateListingPage = lazy(() =>
  import('./pages/CreateListingPage').then((m) => ({ default: m.CreateListingPage })),
)
const PendingListingsPage = lazy(() =>
  import('./pages/admin/PendingListingsPage').then((m) => ({ default: m.PendingListingsPage })),
)
const AgentsPage = lazy(() => import('./pages/admin/AgentsPage').then((m) => ({ default: m.AgentsPage })))
const ProfilePage = lazy(() => import('./pages/ProfilePage').then((m) => ({ default: m.ProfilePage })))

function PageFallback() {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', py: 10 }}>
      <CircularProgress />
    </Box>
  )
}

function NotFound() {
  return (
    <Box sx={{ textAlign: 'center', py: 10 }}>
      <Typography variant="h3" gutterBottom>
        404
      </Typography>
      <Typography color="text.secondary">That page doesn&apos;t exist.</Typography>
    </Box>
  )
}

export default function App() {
  return (
    <Suspense fallback={<PageFallback />}>
      <Routes>
        {/* Full-screen auth pages (no app bar) */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/verify" element={<VerifyEmailPage />} />
        <Route path="/verify-prompt" element={<VerifyPromptPage />} />

        {/* App shell */}
        <Route element={<AppLayout />}>
          <Route path="/" element={<LandingPage />} />
          <Route path="/browse" element={<BrowsePage />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/properties/:id"
            element={
              <ProtectedRoute>
                <PropertyDetailPage />
              </ProtectedRoute>
            }
          />

          {/* Owner + Agent */}
          <Route
            path="/my-properties"
            element={
              <RoleGuard roles={['OWNER', 'AGENT']}>
                <MyPropertiesPage />
              </RoleGuard>
            }
          />
          <Route
            path="/properties/new"
            element={
              <RoleGuard roles={['OWNER', 'AGENT']}>
                <CreatePropertyPage />
              </RoleGuard>
            }
          />
          <Route
            path="/listings/new"
            element={
              <RoleGuard roles={['OWNER', 'AGENT']}>
                <CreateListingPage />
              </RoleGuard>
            }
          />

          {/* Admin */}
          <Route
            path="/admin/listings/pending"
            element={
              <RoleGuard roles={['ADMIN']}>
                <PendingListingsPage />
              </RoleGuard>
            }
          />
          <Route
            path="/admin/agents"
            element={
              <RoleGuard roles={['ADMIN']}>
                <AgentsPage />
              </RoleGuard>
            }
          />

          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </Suspense>
  )
}
