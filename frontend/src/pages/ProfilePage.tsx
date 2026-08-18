import { useEffect, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Grid from '@mui/material/Grid'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { userService } from '../services/user.service'
import { extractErrorMessage } from '../lib/error'
import { formatDate } from '../lib/format'
import { PageHeader } from '../components/ui/PageHeader'
import { RoleChip, UserStatusChip } from '../components/ui/StatusChip'
import { LoadingState } from '../components/ui/StateViews'
import type { User } from '../types/user'

export function ProfilePage() {
  const { user: authUser, setUser } = useAuth()
  const toast = useToast()
  const [profile, setProfile] = useState<User | null>(null)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    if (!authUser) return
    let cancelled = false
    userService
      .getById(authUser.id)
      .then((fresh) => {
        if (cancelled) return
        setProfile(fresh)
        setName(fresh.name)
        setEmail(fresh.email)
      })
      .catch(() => {
        if (!cancelled) {
          setProfile(authUser)
          setName(authUser.name)
          setEmail(authUser.email)
        }
      })
    return () => {
      cancelled = true
    }
  }, [authUser])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!profile) return
    const nextErrors: Record<string, string> = {}
    if (!name.trim()) nextErrors.name = 'Name is required'
    if (!/^\S+@\S+\.\S+$/.test(email)) nextErrors.email = 'Enter a valid email'
    if (password && password.length < 8) nextErrors.password = 'Password must be at least 8 characters'
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setSaving(true)
    setSaved(false)
    try {
      const updated = await userService.update(profile.id, {
        name: name.trim(),
        email,
        password: password.trim() ? password : undefined,
      })
      setProfile(updated)
      setUser(updated)
      setPassword('')
      setSaved(true)
      toast.success('Profile updated.')
    } catch (err) {
      toast.error(extractErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  if (!profile) return <LoadingState label="Loading profile…" />

  return (
    <>
      <PageHeader title="Profile" subtitle="Your account details across all services." />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                {profile.name}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                {profile.email}
              </Typography>
              <Stack direction="row" spacing={1}>
                <RoleChip role={profile.role} />
                <UserStatusChip status={profile.status} />
              </Stack>
              <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
                User ID: {profile.id}
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                Registered: {formatDate(profile.createdAt)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Edit details
              </Typography>
              {saved && (
                <Alert severity="success" sx={{ mb: 2 }}>
                  Changes saved.
                </Alert>
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
                    label="New password (leave blank to keep current)"
                    type="password"
                    fullWidth
                    value={password}
                    error={Boolean(errors.password)}
                    helperText={errors.password}
                    onChange={(e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)}
                  />
                  <Box>
                    <Button type="submit" variant="contained" disabled={saving}>
                      {saving ? 'Saving…' : 'Save changes'}
                    </Button>
                  </Box>
                </Stack>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </>
  )
}
