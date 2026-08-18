import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import FormControl from '@mui/material/FormControl'
import FormHelperText from '@mui/material/FormHelperText'
import Grid from '@mui/material/Grid'
import InputLabel from '@mui/material/InputLabel'
import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import TextField from '@mui/material/TextField'
import type { CreateListingRequest, Property, Purpose } from '../../types/property'

export function ListingForm({
  properties,
  initialPropertyId,
  onSubmit,
  submitLabel = 'Create listing',
}: {
  properties: Property[]
  initialPropertyId?: string
  onSubmit: (payload: CreateListingRequest) => Promise<void>
  submitLabel?: string
}) {
  const [propertyId, setPropertyId] = useState(initialPropertyId ?? '')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [purpose, setPurpose] = useState<Purpose | ''>('')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const nextErrors: Record<string, string> = {}
    if (!propertyId) nextErrors.propertyId = 'Select a property'
    if (!title.trim()) nextErrors.title = 'Title is required'
    if (!purpose) nextErrors.purpose = 'Purpose is required'
    const numericPrice = Number(price)
    if (!price || numericPrice <= 0) nextErrors.price = 'Price must be greater than 0'
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setSubmitting(true)
    try {
      await onSubmit({
        propertyId,
        title: title.trim(),
        description: description.trim() || undefined,
        price: numericPrice,
        purpose: purpose as Purpose,
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12 }}>
          <FormControl fullWidth error={Boolean(errors.propertyId)}>
            <InputLabel>Property</InputLabel>
            <Select label="Property" value={propertyId} onChange={(e) => setPropertyId(e.target.value)}>
              {properties.map((property) => (
                <MenuItem key={property.id} value={property.id}>
                  {property.title} · {property.city}, {property.state}
                </MenuItem>
              ))}
            </Select>
            {errors.propertyId && <FormHelperText>{errors.propertyId}</FormHelperText>}
          </FormControl>
        </Grid>
        <Grid size={{ xs: 12 }}>
          <TextField
            fullWidth
            required
            label="Listing title"
            value={title}
            error={Boolean(errors.title)}
            helperText={errors.title}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setTitle(e.target.value)}
          />
        </Grid>
        <Grid size={{ xs: 12 }}>
          <TextField
            fullWidth
            multiline
            minRows={2}
            label="Description (optional)"
            value={description}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setDescription(e.target.value)}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <FormControl fullWidth error={Boolean(errors.purpose)}>
            <InputLabel>Purpose</InputLabel>
            <Select label="Purpose" value={purpose} onChange={(e) => setPurpose(e.target.value as Purpose)}>
              <MenuItem value="FOR_RENT">For rent</MenuItem>
              <MenuItem value="FOR_SALE">For sale</MenuItem>
            </Select>
            {errors.purpose && <FormHelperText>{errors.purpose}</FormHelperText>}
          </FormControl>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            fullWidth
            required
            type="number"
            label="Price (₦)"
            value={price}
            error={Boolean(errors.price)}
            helperText={errors.price}
            onChange={(e: ChangeEvent<HTMLInputElement>) => setPrice(e.target.value)}
          />
        </Grid>
        <Grid size={{ xs: 12 }}>
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? <CircularProgress size={20} color="inherit" /> : submitLabel}
          </Button>
        </Grid>
      </Grid>
    </Box>
  )
}
