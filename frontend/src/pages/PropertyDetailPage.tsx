import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import CardMedia from '@mui/material/CardMedia'
import Chip from '@mui/material/Chip'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControl from '@mui/material/FormControl'
import Grid from '@mui/material/Grid'
import InputLabel from '@mui/material/InputLabel'
import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import HomeWorkIcon from '@mui/icons-material/HomeWork'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { listingService } from '../services/listing.service'
import { propertyService } from '../services/property.service'
import { extractErrorMessage } from '../lib/error'
import { formatPrice } from '../lib/format'
import { ListingCard } from '../components/listing/ListingCard'
import { PageHeader } from '../components/ui/PageHeader'
import { ErrorState, LoadingState } from '../components/ui/StateViews'
import { AvailabilityStatusChip } from '../components/ui/StatusChip'
import type { AvailabilityStatus, Listing, Property } from '../types/property'

const AVAILABILITY_OPTIONS: AvailabilityStatus[] = ['AVAILABLE', 'LEASED', 'SOLD', 'UNAVAILABLE', 'UNDER_MAINTENANCE']

export function PropertyDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { user } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const [property, setProperty] = useState<Property | null>(null)
  const [listings, setListings] = useState<Listing[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [availabilityOpen, setAvailabilityOpen] = useState(false)
  const [availabilityStatus, setAvailabilityStatus] = useState<AvailabilityStatus | ''>('')
  const [statusSaving, setStatusSaving] = useState(false)

  const [agentOpen, setAgentOpen] = useState(false)
  const [agentId, setAgentId] = useState('')
  const [agentSaving, setAgentSaving] = useState(false)

  const isOwner = user?.id === property?.ownerId
  const isAgentOf = user?.role === 'AGENT' && user?.id === property?.agentId
  const canManage = isOwner || isAgentOf
  const canAssignAgent = isOwner
  const canChangeStatus = isOwner || user?.role === 'ADMIN'

  const load = () => {
    if (!id) return
    setError(null)
    setProperty(null)
    Promise.all([propertyService.getById(id), listingService.getByProperty(id)])
      .then(([propertyData, listingData]) => {
        setProperty(propertyData)
        setListings(listingData)
      })
      .catch((err: unknown) => setError(extractErrorMessage(err)))
  }

  useEffect(load, [id])

  const handleChangeStatus = async () => {
    if (!property || !availabilityStatus) return
    setStatusSaving(true)
    try {
      const updated = await propertyService.updateStatus(property.id, availabilityStatus as AvailabilityStatus)
      setProperty(updated)
      setAvailabilityOpen(false)
      toast.success('Availability status updated.')
    } catch (err) {
      toast.error(extractErrorMessage(err))
    } finally {
      setStatusSaving(false)
    }
  }

  const handleAssignAgent = async () => {
    if (!property) return
    setAgentSaving(true)
    try {
      const updated = await propertyService.assignAgent(property.id, agentId.trim())
      setProperty(updated)
      setAgentOpen(false)
      setAgentId('')
      toast.success('Agent assigned to this property.')
    } catch (err) {
      toast.error(extractErrorMessage(err))
    } finally {
      setAgentSaving(false)
    }
  }

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!property || !listings) return <LoadingState label="Loading property…" />

  const image = property.imageUrls?.[0]

  return (
    <>
      <PageHeader
        title={property.title}
        subtitle="Property details and its listings."
        action={
          canManage ? (
            <Button variant="contained" onClick={() => navigate(`/listings/new?propertyId=${property.id}`)}>
              Create listing
            </Button>
          ) : undefined
        }
      />

      <Card sx={{ mb: 3 }}>
        {image ? (
          <CardMedia component="img" image={image} alt={property.title} sx={{ height: 320, objectFit: 'cover' }} />
        ) : (
          <Box sx={{ height: 320, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: '#0f4c5c', color: 'white' }}>
            <HomeWorkIcon sx={{ fontSize: 96, opacity: 0.8 }} />
          </Box>
        )}
        <CardContent>
          <Stack direction="row" spacing={2} sx={{ mb: 2, justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h5" color="primary.main">
              {formatPrice(property.price)}
            </Typography>
            <AvailabilityStatusChip status={property.availabilityStatus} />
          </Stack>

          <Stack direction="row" spacing={0.5} sx={{ color: 'text.secondary', mb: 2, alignItems: 'center' }}>
            <LocationOnIcon fontSize="small" />
            <Typography variant="body1">{property.formattedAddress ?? `${property.address}, ${property.city}, ${property.state}`}</Typography>
          </Stack>

          <Grid container spacing={1} sx={{ mb: 2 }}>
            <Grid size={{ xs: 6, sm: 3 }}>
              <Chip label={property.propertyType.replace('_', ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase())} />
            </Grid>
            {property.bedrooms !== undefined && (
              <Grid size={{ xs: 6, sm: 3 }}>
                <Chip label={`${property.bedrooms} bed`} />
              </Grid>
            )}
            {property.bathrooms !== undefined && (
              <Grid size={{ xs: 6, sm: 3 }}>
                <Chip label={`${property.bathrooms} bath`} />
              </Grid>
            )}
            {property.squareFootage !== undefined && (
              <Grid size={{ xs: 6, sm: 3 }}>
                <Chip label={`${property.squareFootage} sqft`} />
              </Grid>
            )}
          </Grid>

          {property.description && (
            <Typography variant="body1" sx={{ mb: 2 }}>
              {property.description}
            </Typography>
          )}

          <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
            Address verified: {property.addressVerified ? 'Yes' : 'No'} · Owner ID: {property.ownerId}
            {property.agentId ? ` · Agent ID: ${property.agentId}` : ''}
          </Typography>

          {canManage && (
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} sx={{ mt: 2 }}>
              {canChangeStatus && (
                <Button variant="outlined" onClick={() => { setAvailabilityStatus(''); setAvailabilityOpen(true) }}>
                  Change availability
                </Button>
              )}
              {canAssignAgent && (
                <Button variant="outlined" onClick={() => { setAgentId(''); setAgentOpen(true) }}>
                  {property.agentId ? 'Change agent' : 'Assign agent'}
                </Button>
              )}
            </Stack>
          )}
        </CardContent>
      </Card>

      <Typography variant="h6" gutterBottom>
        Listings for this property
      </Typography>
      {listings.length === 0 ? (
        <Alert severity="info" sx={{ mb: 2 }}>
          No listings for this property yet. {canManage ? 'Create one to put it on the market.' : ''}
        </Alert>
      ) : (
        <Grid container spacing={2}>
          {listings.map((listing) => (
            <Grid key={listing.id} size={{ xs: 12, sm: 6, md: 4 }}>
              <ListingCard listing={listing} />
            </Grid>
          ))}
        </Grid>
      )}

      {/* Change availability */}
      <Dialog open={availabilityOpen} onClose={() => setAvailabilityOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>Change availability</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 1 }}>
            <InputLabel>Availability status</InputLabel>
            <Select label="Availability status" value={availabilityStatus} onChange={(e) => setAvailabilityStatus(e.target.value as AvailabilityStatus)}>
              {AVAILABILITY_OPTIONS.map((option) => (
                <MenuItem key={option} value={option}>
                  {option.replace('_', ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase())}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAvailabilityOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleChangeStatus} disabled={!availabilityStatus || statusSaving}>
            Save
          </Button>
        </DialogActions>
      </Dialog>

      {/* Assign agent */}
      <Dialog open={agentOpen} onClose={() => setAgentOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>{property.agentId ? 'Change agent' : 'Assign an agent'}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Agent user ID"
            value={agentId}
            onChange={(e) => setAgentId(e.target.value)}
            sx={{ mt: 1 }}
          />
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
            Paste the agent&apos;s user ID. Agents can be looked up by an admin under Agents.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAgentOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleAssignAgent} disabled={!agentId.trim() || agentSaving}>
            Assign
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}
