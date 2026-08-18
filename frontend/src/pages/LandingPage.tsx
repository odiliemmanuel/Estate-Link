import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Container from '@mui/material/Container'
import Grid from '@mui/material/Grid'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import HomeWorkIcon from '@mui/icons-material/HomeWork'
import KeyIcon from '@mui/icons-material/Key'
import ManageSearchIcon from '@mui/icons-material/ManageSearch'
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const features = [
  {
    icon: <HomeWorkIcon fontSize="large" />,
    title: 'List your property',
    text: 'Owners create properties with full details, photos and an automatically verified address.',
  },
  {
    icon: <KeyIcon fontSize="large" />,
    title: 'Rent or buy',
    text: 'Applicants browse approved listings and express interest in renting or buying.',
  },
  {
    icon: <ManageSearchIcon fontSize="large" />,
    title: 'Agents manage it all',
    text: 'Agents manage listings for owners, schedule inspections and send offers.',
  },
  {
    icon: <VerifiedUserIcon fontSize="large" />,
    title: 'Admin approval',
    text: 'Admins approve listings before they go live and manage agents and platform activity.',
  },
]

export function LandingPage() {
  const { isAuthenticated } = useAuth()
  const [params] = useSearchParams()

  // The notification service emails a verification link as /?token=... —
  // hand it off to the dedicated verify route.
  const token = params.get('token')
  if (token) {
    return <Navigate to={`/verify?token=${encodeURIComponent(token)}`} replace />
  }

  return (
    <Box>
      <Box
        sx={{
          background:
            'radial-gradient(1200px 480px at 78% -10%, rgba(227,100,20,0.35) 0%, rgba(227,100,20,0) 55%),' +
            'linear-gradient(150deg, #082f3b 0%, #0f4c5c 60%, #14555f 100%)',
          color: 'white',
          py: { xs: 9, md: 14 },
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <Container maxWidth="md" sx={{ textAlign: 'center', position: 'relative', zIndex: 1 }}>
          <Typography
            variant="overline"
            component="p"
            sx={{ color: '#c9a86a', mb: 2, letterSpacing: '0.22em' }}
          >
            The premium property experience
          </Typography>
          <Typography
            variant="h2"
            component="h1"
            gutterBottom
            sx={{ fontWeight: 700, textShadow: '0 2px 24px rgba(0,0,0,0.25)' }}
          >
            EstateLink
          </Typography>
          <Typography variant="h6" component="p" sx={{ mb: 4, opacity: 0.92, fontWeight: 400, fontFamily: 'inherit' }}>
            The property management platform that connects owners, agents, applicants and admins —
            powered by Spring Boot microservices and Kafka.
          </Typography>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ justifyContent: 'center' }}>
            <Button
              component={Link}
              to="/browse"
              variant="contained"
              color="secondary"
              size="large"
              sx={{ px: 4, py: 1.2, borderRadius: 999 }}
            >
              Browse listings
            </Button>
            {!isAuthenticated && (
              <Button
                component={Link}
                to="/register"
                variant="outlined"
                size="large"
                sx={{
                  color: 'white',
                  borderColor: 'rgba(255,255,255,0.55)',
                  borderRadius: 999,
                  px: 4,
                  '&:hover': { borderColor: '#fff', bgcolor: 'rgba(255,255,255,0.08)' },
                }}
              >
                Create an account
              </Button>
            )}
          </Stack>
        </Container>
      </Box>

      <Container sx={{ py: 7 }}>
        <Typography variant="overline" align="center" sx={{ color: 'secondary.main', display: 'block', mb: 1 }}>
          One platform, four roles
        </Typography>
        <Typography variant="h4" align="center" gutterBottom>
          Designed around how property moves
        </Typography>
        <Grid container spacing={3} sx={{ mt: 1 }}>
          {features.map((feature) => (
            <Grid key={feature.title} size={{ xs: 12, sm: 6, md: 3 }}>
              <Card sx={{ height: '100%', bgcolor: 'background.paper' }}>
                <CardContent sx={{ p: 3 }}>
                  <Box sx={{ color: 'primary.main', mb: 1.5 }}>{feature.icon}</Box>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600, fontFamily: '"Playfair Display", Georgia, serif' }} gutterBottom>
                    {feature.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.7 }}>
                    {feature.text}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>
    </Box>
  )
}
