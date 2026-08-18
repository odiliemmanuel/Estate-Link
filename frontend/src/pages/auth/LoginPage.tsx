import { useState } from 'react'
import type { ChangeEvent, FormEvent, ReactNode } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import CircularProgress from '@mui/material/CircularProgress'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import HomeWorkIcon from '@mui/icons-material/HomeWork'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { extractErrorMessage } from '../../lib/error'

export function LoginPage() {
  const { isAuthenticated, login } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const from = (location.state as { from?: string } | null)?.from ?? '/dashboard'

  if (isAuthenticated) return <Navigate to="/dashboard" replace />

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const user = await login(email, password)
      toast.success(`Welcome back, ${user.name}!`)
      navigate(from, { replace: true })
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthShell title="Welcome back" subtitle="Sign in to continue to EstateLink">
      {error && (
        <Box sx={{ mb: 2 }}>
          <AlertBanner message={error} />
        </Box>
      )}
      <Box component="form" onSubmit={handleSubmit} noValidate>
        <Stack spacing={2}>
          <TextField
            label="Email"
            type="email"
            required
            fullWidth
            value={email}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)}
          />
          <TextField
            label="Password"
            type="password"
            required
            fullWidth
            value={password}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)}
          />
          <Button type="submit" variant="contained" size="large" fullWidth disabled={submitting}>
            {submitting ? <CircularProgress size={22} color="inherit" /> : 'Sign in'}
          </Button>
        </Stack>
      </Box>
      <Typography variant="body2" sx={{ mt: 2 }} align="center">
        Don&apos;t have an account?{' '}
        <Button component={Link} to="/register" size="small">
          Register
        </Button>
      </Typography>
    </AuthShell>
  )
}

function AlertBanner({ message }: { message: string }) {
  return (
    <Box
      sx={{
        p: 1.5,
        borderRadius: 1,
        backgroundColor: 'error.light',
        color: 'error.dark',
        fontSize: '0.875rem',
      }}
    >
      {message}
    </Box>
  )
}

export function AuthShell({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#0f4c5c',
        p: 2,
      }}
    >
      <Card sx={{ width: '100%', maxWidth: 440 }}>
        <CardContent sx={{ p: 4 }}>
          <Stack spacing={1} sx={{ mb: 3, alignItems: 'center' }}>
            <HomeWorkIcon sx={{ fontSize: 44, color: 'primary.main' }} />
            <Typography variant="h5">EstateLink</Typography>
          </Stack>
          <Typography variant="h6" gutterBottom>
            {title}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {subtitle}
          </Typography>
          {children}
        </CardContent>
      </Card>
    </Box>
  )
}
