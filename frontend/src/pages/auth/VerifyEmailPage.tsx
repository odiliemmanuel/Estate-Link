import { useEffect, useState } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import ErrorIcon from '@mui/icons-material/Error'
import { Link, useSearchParams } from 'react-router-dom'
import { authService } from '../../services/auth.service'
import { extractErrorMessage } from '../../lib/error'
import { AuthShell } from './LoginPage'

export function VerifyEmailPage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const [state, setState] = useState<'loading' | 'success' | 'error'>('loading')
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (!token) {
      setState('error')
      setMessage('No verification token was provided. Check the link in your email.')
      return
    }
    let cancelled = false
    authService
      .verifyEmail(token)
      .then(() => {
        if (!cancelled) setState('success')
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setState('error')
          setMessage(extractErrorMessage(err))
        }
      })
    return () => {
      cancelled = true
    }
  }, [token])

  return (
    <AuthShell title="Email verification" subtitle="Confirm your EstateLink account">
      <Stack spacing={2} sx={{ py: 2, alignItems: 'center' }}>
        {state === 'loading' && <CircularProgress />}
        {state === 'success' && (
          <>
            <CheckCircleIcon color="success" sx={{ fontSize: 56 }} />
            <Typography variant="body1" align="center">
              {message || 'Email verified successfully. You can now log in.'}
            </Typography>
            <Button component={Link} to="/login" variant="contained">
              Go to sign in
            </Button>
          </>
        )}
        {state === 'error' && (
          <>
            <ErrorIcon color="error" sx={{ fontSize: 56 }} />
            <Typography variant="body1" align="center">
              {message}
            </Typography>
          </>
        )}
      </Stack>
    </AuthShell>
  )
}

export function VerifyPromptPage() {
  return (
    <AuthShell title="Verify your email" subtitle="One more step to activate your account">
      <Stack spacing={2} sx={{ py: 2, alignItems: 'center' }}>
        <Box
          sx={{
            width: 52,
            height: 52,
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'warning.light',
          }}
        >
          <Typography variant="h5">✉</Typography>
        </Box>
        <Typography variant="body1" align="center">
          We sent a verification link to your inbox. Open it to activate your account — it expires in
          24 hours. You can&apos;t sign in until your email is verified.
        </Typography>
        <Button component={Link} to="/login" variant="outlined">
          Back to sign in
        </Button>
      </Stack>
    </AuthShell>
  )
}
