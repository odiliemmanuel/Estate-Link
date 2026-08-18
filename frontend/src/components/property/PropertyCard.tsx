import Card from '@mui/material/Card'
import CardActionArea from '@mui/material/CardActionArea'
import CardContent from '@mui/material/CardContent'
import CardMedia from '@mui/material/CardMedia'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import Box from '@mui/material/Box'
import HomeWorkIcon from '@mui/icons-material/HomeWork'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import { Link } from 'react-router-dom'
import { formatPrice } from '../../lib/format'
import type { Property } from '../../types/property'
import { AvailabilityStatusChip } from '../ui/StatusChip'

export function PropertyCard({ property }: { property: Property }) {
  const image = property.imageUrls?.[0]

  return (
    <Card>
      <CardActionArea component={Link} to={`/properties/${property.id}`}>
        {image ? (
          <CardMedia
            component="img"
            image={image}
            alt={property.title}
            sx={{ height: 180, objectFit: 'cover', backgroundColor: '#e5e7eb' }}
          />
        ) : (
          <Box
            sx={{
              height: 180,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: '#0f4c5c',
              color: 'white',
            }}
          >
            <HomeWorkIcon sx={{ fontSize: 64, opacity: 0.8 }} />
          </Box>
        )}
        <CardContent>
          <Stack direction="row" spacing={1} sx={{ justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }} noWrap>
              {property.title}
            </Typography>
            <AvailabilityStatusChip status={property.availabilityStatus} />
          </Stack>
          <Stack direction="row" spacing={0.5} sx={{ mt: 0.5, color: 'text.secondary', alignItems: 'center' }}>
            <LocationOnIcon fontSize="small" />
            <Typography variant="body2" noWrap>
              {property.city}, {property.state}
            </Typography>
          </Stack>
          <Typography variant="h6" color="primary.main" sx={{ mt: 1 }}>
            {formatPrice(property.price)}
          </Typography>
        </CardContent>
      </CardActionArea>
    </Card>
  )
}
