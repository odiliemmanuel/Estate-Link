import { useEffect, useState } from 'react'
import type { ReactElement } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Grid from '@mui/material/Grid'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import AddIcon from '@mui/icons-material/Add'
import ApartmentIcon from '@mui/icons-material/Apartment'
import ApprovalsIcon from '@mui/icons-material/TaskAlt'
import GroupsIcon from '@mui/icons-material/Groups'
import StorefrontIcon from '@mui/icons-material/Storefront'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { propertyService } from '../services/property.service'
import { listingService } from '../services/listing.service'
import { userService } from '../services/user.service'
import { LoadingState } from '../components/ui/StateViews'

interface Stat {
  label: string
  value: number
  icon: ReactElement
  to?: string
}

export function DashboardPage() {
  const { user } = useAuth()
  const [stats, setStats] = useState<Stat[] | null>(null)

  useEffect(() => {
    if (!user) return
    let cancelled = false

    const load = async () => {
      let next: Stat[]
      if (user.role === 'OWNER' || user.role === 'AGENT') {
        const properties = await propertyService.getMine()
        next = [
          {
            label: 'Properties',
            value: properties.length,
            icon: <ApartmentIcon />,
            to: '/my-properties',
          },
          { label: 'Active availability', value: properties.filter((p) => p.availabilityStatus === 'AVAILABLE').length, icon: <StorefrontIcon />, to: '/my-properties' },
        ]
      } else if (user.role === 'ADMIN') {
        const [pending, agents] = await Promise.all([
          listingService.getPending(),
          userService.getAllAgents(),
        ])
        next = [
          { label: 'Pending approvals', value: pending.length, icon: <ApprovalsIcon />, to: '/admin/listings/pending' },
          { label: 'Agents', value: agents.length, icon: <GroupsIcon />, to: '/admin/agents' },
        ]
      } else {
        const active = await listingService.getActive()
        next = [{ label: 'Live listings', value: active.length, icon: <StorefrontIcon />, to: '/browse' }]
      }
      if (!cancelled) setStats(next)
    }

    load().catch(() => {
      if (!cancelled) setStats([])
    })
    return () => {
      cancelled = true
    }
  }, [user])

  if (!user) return null
  if (!stats) return <LoadingState label="Loading your dashboard…" />

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Welcome, {user.name}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Here&apos;s what&apos;s happening across your workspace.
      </Typography>

      <Grid container spacing={3}>
        {stats.map((stat) => (
          <Grid key={stat.label} size={{ xs: 12, sm: 6, md: 4 }}>
            <Card>
              <CardContent>
                <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
                  <Box sx={{ color: 'primary.main' }}>{stat.icon}</Box>
                  <Box>
                    <Typography variant="h4">{stat.value}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {stat.label}
                    </Typography>
                  </Box>
                </Stack>
                {stat.to && (
                  <Button component={Link} to={stat.to} size="small" sx={{ mt: 1 }}>
                    View
                  </Button>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {(user.role === 'OWNER' || user.role === 'AGENT') && (
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mt: 4 }}>
          <Button component={Link} to="/properties/new" variant="contained" startIcon={<AddIcon />}>
            Add property
          </Button>
          <Button component={Link} to="/listings/new" variant="outlined" startIcon={<AddIcon />}>
            Create listing
          </Button>
        </Stack>
      )}
      {user.role === 'APPLICANT' && (
        <Button component={Link} to="/browse" variant="contained" sx={{ mt: 4 }}>
          Browse listings
        </Button>
      )}
    </Box>
  )
}
