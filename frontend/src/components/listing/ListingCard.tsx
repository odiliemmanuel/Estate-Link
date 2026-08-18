import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import IconButton from '@mui/material/IconButton'
import Link from '@mui/material/Link'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import EditIcon from '@mui/icons-material/Edit'
import HomeWorkIcon from '@mui/icons-material/HomeWork'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import { formatPrice } from '../../lib/format'
import type { Listing, Property } from '../../types/property'
import { ListingStatusChip, PurposeChip } from '../ui/StatusChip'

interface ListingCardProps {
  listing: Listing
  property?: Property
  onEdit?: (listing: Listing, property?: Property) => void
}

export function ListingCard({ listing, property, onEdit }: ListingCardProps) {
  const image = property?.imageUrls?.[0]
  const location = property ? [property.city, property.state].filter(Boolean).join(', ') : null

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
      <Box sx={{ position: 'relative' }}>
        <Link
          href={`/properties/${listing.propertyId}`}
          underline="none"
          sx={{ position: 'relative', display: 'block', height: 180 }}
        >
          {image ? (
            <Box component="img" src={image} alt={listing.title} className="estatelink-media" />
          ) : (
            <Box
              sx={{
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background:
                  'linear-gradient(135deg, #0f4c5c 0%, #0a3542 60%, #14555f 100%)',
                color: 'rgba(255,255,255,0.85)',
              }}
            >
              <HomeWorkIcon sx={{ fontSize: 64 }} />
            </Box>
          )}
          <Box sx={{ position: 'absolute', top: 10, left: 10 }}>
            <PurposeChip purpose={listing.purpose} />
          </Box>
        </Link>

        {onEdit && (
          <IconButton
            size="small"
            aria-label="Edit listing"
            onClick={() => onEdit(listing, property)}
            sx={{
              position: 'absolute',
              top: 10,
              right: 10,
              bgcolor: 'rgba(0,0,0,0.45)',
              color: '#fff',
              zIndex: 2,
              '&:hover': { bgcolor: 'rgba(0,0,0,0.65)' },
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
        )}
      </Box>

      <CardContent sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', pt: 2 }}>
        <Stack direction="row" spacing={1} sx={{ justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <Typography variant="h6" component="h3" sx={{ fontWeight: 600, mb: 0.5 }}>
            {listing.title}
          </Typography>
          <ListingStatusChip status={listing.status} />
        </Stack>
        {location && (
          <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center', mb: 1, color: 'text.secondary' }}>
            <LocationOnIcon fontSize="small" sx={{ opacity: 0.7 }} />
            <Typography variant="body2" color="text.secondary">
              {location}
            </Typography>
          </Stack>
        )}
        {listing.description && (
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{
              mt: 0.5,
              overflow: 'hidden',
              display: '-webkit-box',
              WebkitLineClamp: 2,
              WebkitBoxOrient: 'vertical',
            }}
          >
            {listing.description}
          </Typography>
        )}
        <Typography
          variant="h5"
          sx={{ mt: 'auto', pt: 1.5, color: 'secondary.main', fontFamily: '"Playfair Display", Georgia, serif', fontWeight: 700 }}
        >
          {formatPrice(listing.price)}
        </Typography>
      </CardContent>
    </Card>
  )
}
