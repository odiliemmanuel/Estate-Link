import { useState } from 'react'
import type { MouseEvent } from 'react'
import AppBar from '@mui/material/AppBar'
import Avatar from '@mui/material/Avatar'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Container from '@mui/material/Container'
import IconButton from '@mui/material/IconButton'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'
import Toolbar from '@mui/material/Toolbar'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import DarkModeIcon from '@mui/icons-material/DarkMode'
import LightModeIcon from '@mui/icons-material/LightMode'
import LogoutIcon from '@mui/icons-material/Logout'
import PersonIcon from '@mui/icons-material/Person'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useThemeMode } from '../../context/ThemeModeContext'
import { RoleChip } from '../ui/StatusChip'
import type { Role } from '../../types/user'

interface NavItem {
  to: string
  label: string
}

const authNav: Record<Role, NavItem[]> = {
  OWNER: [
    { to: '/dashboard', label: 'Dashboard' },
    { to: '/my-properties', label: 'My Properties' },
    { to: '/listings/new', label: 'Create Listing' },
  ],
  AGENT: [
    { to: '/dashboard', label: 'Dashboard' },
    { to: '/my-properties', label: 'Managed Properties' },
    { to: '/listings/new', label: 'Create Listing' },
  ],
  APPLICANT: [{ to: '/dashboard', label: 'Dashboard' }],
  ADMIN: [
    { to: '/dashboard', label: 'Dashboard' },
    { to: '/admin/listings/pending', label: 'Pending Listings' },
    { to: '/admin/agents', label: 'Agents' },
  ],
}

const publicNav: NavItem[] = [{ to: '/browse', label: 'Browse Listings' }]

export function AppLayout() {
  const { user, isAuthenticated, logout } = useAuth()
  const { mode, toggleMode } = useThemeMode()
  const navigate = useNavigate()
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null)

  const handleOpenMenu = (event: MouseEvent<HTMLElement>) => setAnchorEl(event.currentTarget)
  const handleCloseMenu = () => setAnchorEl(null)

  const navItems: NavItem[] = isAuthenticated && user ? authNav[user.role] : publicNav

  const handleLogout = () => {
    handleCloseMenu()
    logout()
    navigate('/')
  }

  const renderNavLink = ({ to, label }: NavItem) => (
    <Button
      key={to}
      component={NavLink}
      to={to}
      sx={{
        color: 'inherit',
        '&.active': { backgroundColor: 'rgba(255,255,255,0.14)', color: '#fff' },
      }}
    >
      {label}
    </Button>
  )

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar
        position="sticky"
        sx={{
          background: 'linear-gradient(120deg, #0a3542 0%, #0f4c5c 55%, #134d5a 100%)',
          color: '#fff',
        }}
      >
        <Toolbar>
          <Button component={Link} to="/" sx={{ color: 'inherit', mr: 3, minWidth: 0 }}>
            <Typography
              variant="h6"
              component="span"
              sx={{ fontFamily: '"Playfair Display", Georgia, serif', fontWeight: 700, letterSpacing: '0.02em' }}
            >
              EstateLink
            </Typography>
          </Button>

          <Box sx={{ flexGrow: 1 }} />

          {navItems.map(renderNavLink)}

          <Tooltip title={mode === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}>
            <IconButton onClick={toggleMode} sx={{ ml: 1, color: 'inherit' }}>
              {mode === 'dark' ? <LightModeIcon /> : <DarkModeIcon />}
            </IconButton>
          </Tooltip>

          {isAuthenticated && user ? (
            <>
              <Tooltip title="Account">
                <IconButton onClick={handleOpenMenu} sx={{ ml: 1, color: 'inherit' }}>
                  <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main' }}>
                    {user.name.charAt(0).toUpperCase()}
                  </Avatar>
                </IconButton>
              </Tooltip>
              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleCloseMenu}
                slotProps={{ paper: { sx: { minWidth: 220, p: 1 } } }}
              >
                <Box sx={{ px: 2, py: 1 }}>
                  <Typography variant="subtitle2">{user.name}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {user.email}
                  </Typography>
                  <Box sx={{ mt: 0.5 }}>
                    <RoleChip role={user.role} />
                  </Box>
                </Box>
                <MenuItem onClick={() => { handleCloseMenu(); navigate('/profile') }}>
                  <PersonIcon sx={{ mr: 1 }} fontSize="small" />
                  Profile
                </MenuItem>
                <MenuItem onClick={handleLogout}>
                  <LogoutIcon sx={{ mr: 1 }} fontSize="small" />
                  Sign out
                </MenuItem>
              </Menu>
            </>
          ) : (
            <>
              <Button component={Link} to="/login" sx={{ color: 'inherit', ml: 1 }}>
                Sign in
              </Button>
              <Button component={Link} to="/register" variant="contained" color="secondary" sx={{ ml: 1 }}>
                Get started
              </Button>
            </>
          )}
        </Toolbar>
      </AppBar>

      <Container component="main" maxWidth="lg" sx={{ py: 4, flexGrow: 1 }}>
        <Outlet />
      </Container>

      <Box component="footer" sx={{ py: 4, textAlign: 'center', borderTop: '1px solid', borderColor: 'divider' }}>
        <Typography variant="overline" color="text.secondary">
          EstateLink · Property management platform
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
          @Odili @Fola Java Springboot · Microservices · Kafka · React
        </Typography>
      </Box>
    </Box>
  )
}
