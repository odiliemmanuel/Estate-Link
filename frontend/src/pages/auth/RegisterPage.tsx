import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import FormControl from '@mui/material/FormControl'
import FormHelperText from '@mui/material/FormHelperText'
import InputLabel from '@mui/material/InputLabel'
import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { Link, useNavigate } from 'react-router-dom'
import { authService } from '../../services/auth.service'
import { useToast } from '../../context/ToastContext'
import { extractErrorMessage } from '../../lib/error'
import { AuthShell } from './LoginPage'
import type { Role } from '../../types/user'

const ROLES: Role[] = ['OWNER', 'AGENT', 'APPLICANT']

export function RegisterPage() {
  const toast = useToast()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<Role | ''>('')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const nextErrors: Record<string, string> = {}
    if (!name.trim()) nextErrors.name = 'Name is required'
    if (!/^\S+@\S+\.\S+$/.test(email)) nextErrors.email = 'Enter a valid email'
    if (password.length < 8) nextErrors.password = 'Password must be at least 8 characters'
    if (!role) nextErrors.role = 'Select a role'
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setSubmitting(true)
    setServerError(null)
    try {
      await authService.register({ name: name.trim(), email, password, role: role as Role })
      toast.success('Account created! Check your email to verify it.')
      navigate('/verify-prompt', { replace: true })
    } catch (err) {
      setServerError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthShell title="Create your account" subtitle="Join EstateLink as an owner, agent or applicant">
      {serverError && (
        <Box sx={{ mb: 2 }}>
          <Typography
            variant="body2"
            sx={{
              p: 1.5,
              borderRadius: 1,
              backgroundColor: 'error.light',
              color: 'error.dark',
            }}
          >
            {serverError}
          </Typography>
        </Box>
      )}
      <Box component="form" onSubmit={handleSubmit} noValidate>
        <Stack spacing={2}>
          <TextField
            label="Full name"
            required
            fullWidth
            value={name}
            error={Boolean(errors.name)}
            helperText={errors.name}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setName(e.target.value)}
          />
          <TextField
            label="Email"
            type="email"
            required
            fullWidth
            value={email}
            error={Boolean(errors.email)}
            helperText={errors.email}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)}
          />
          <TextField
            label="Password"
            type="password"
            required
            fullWidth
            value={password}
            error={Boolean(errors.password)}
            helperText={errors.password}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)}
          />
          <FormControl fullWidth error={Boolean(errors.role)}>
            <InputLabel>I am a…</InputLabel>
            <Select label="I am a…" value={role} onChange={(e) => setRole(e.target.value as Role)}>
              {ROLES.map((r) => (
                <MenuItem key={r} value={r}>
                  {r.charAt(0) + r.slice(1).toLowerCase()}
                </MenuItem>
              ))}
            </Select>
            {errors.role && <FormHelperText>{errors.role}</FormHelperText>}
          </FormControl>
          <Button type="submit" variant="contained" size="large" fullWidth disabled={submitting}>
            {submitting ? 'Creating account…' : 'Register'}
          </Button>
        </Stack>
      </Box>
      <Typography variant="body2" sx={{ mt: 2 }} align="center">
        Already registered?{' '}
        <Button component={Link} to="/login" size="small">
          Sign in
        </Button>
      </Typography>
    </AuthShell>
  )
}
