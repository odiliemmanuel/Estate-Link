import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import FormControl from '@mui/material/FormControl'
import Grid from '@mui/material/Grid'
import InputLabel from '@mui/material/InputLabel'
import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import type { CreatePropertyRequest, PropertyType } from '../../types/property'
import { titleCase } from '../../lib/format'
import { ImagePicker } from './ImagePicker'

const PROPERTY_TYPES: PropertyType[] = ['RESIDENTIAL', 'COMMERCIAL', 'INDUSTRIAL', 'RETAIL', 'SPECIAL_PURPOSE']

interface PropertyFormValues {
  title: string
  description: string
  address: string
  city: string
  state: string
  propertyType: PropertyType | ''
  price: string
  bedrooms: string
  bathrooms: string
  squareFootage: string
  images: string[]
}

const initialValues: PropertyFormValues = {
  title: '',
  description: '',
  address: '',
  city: '',
  state: '',
  propertyType: '',
  price: '',
  bedrooms: '',
  bathrooms: '',
  squareFootage: '',
  images: [],
}

export function PropertyForm({
  onSubmit,
  submitLabel = 'Create property',
}: {
  onSubmit: (payload: CreatePropertyRequest) => Promise<void>
  submitLabel?: string
}) {
  const [values, setValues] = useState<PropertyFormValues>(initialValues)
  const [submitting, setSubmitting] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})

  const setField = (field: keyof PropertyFormValues, value: string) => {
    setValues((prev) => ({ ...prev, [field]: value }))
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const nextErrors: Record<string, string> = {}
    if (!values.title.trim()) nextErrors.title = 'Title is required'
    if (!values.address.trim()) nextErrors.address = 'Address is required'
    if (!values.city.trim()) nextErrors.city = 'City is required'
    if (!values.state.trim()) nextErrors.state = 'State is required'
    if (!values.propertyType) nextErrors.propertyType = 'Property type is required'
    const price = Number(values.price)
    if (!values.price || price <= 0) nextErrors.price = 'Price must be greater than 0'
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setSubmitting(true)
    try {
      await onSubmit({
        title: values.title.trim(),
        description: values.description.trim() || undefined,
        address: values.address.trim(),
        city: values.city.trim(),
        state: values.state.trim(),
        propertyType: values.propertyType as PropertyType,
        price,
        bedrooms: values.bedrooms ? Number(values.bedrooms) : undefined,
        bathrooms: values.bathrooms ? Number(values.bathrooms) : undefined,
        squareFootage: values.squareFootage ? Number(values.squareFootage) : undefined,
        imageUrls: values.images,
      })
    } finally {
      setSubmitting(false)
    }
  }

  const textFieldProps = (field: keyof PropertyFormValues, label: string) => ({
    label,
    value: values[field],
    error: Boolean(errors[field]),
    helperText: errors[field],
    onChange: (e: ChangeEvent<HTMLInputElement>) => setField(field, e.target.value),
  })

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12 }}>
          <TextField fullWidth required {...textFieldProps('title', 'Property title')} />
        </Grid>
        <Grid size={{ xs: 12 }}>
          <TextField
            fullWidth
            multiline
            minRows={2}
            {...textFieldProps('description', 'Description (optional)')}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField fullWidth required {...textFieldProps('address', 'Street address')} />
        </Grid>
        <Grid size={{ xs: 12, sm: 3 }}>
          <TextField fullWidth required {...textFieldProps('city', 'City')} />
        </Grid>
        <Grid size={{ xs: 12, sm: 3 }}>
          <TextField fullWidth required {...textFieldProps('state', 'State')} />
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <FormControl fullWidth error={Boolean(errors.propertyType)}>
            <InputLabel>Property type</InputLabel>
            <Select
              label="Property type"
              value={values.propertyType}
              onChange={(e) => setField('propertyType', e.target.value)}
            >
              {PROPERTY_TYPES.map((type) => (
                <MenuItem key={type} value={type}>
                  {titleCase(type)}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <TextField fullWidth required type="number" {...textFieldProps('price', 'Price (₦)')} />
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <TextField fullWidth type="number" {...textFieldProps('bedrooms', 'Bedrooms')} />
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <TextField fullWidth type="number" {...textFieldProps('bathrooms', 'Bathrooms')} />
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <TextField fullWidth type="number" {...textFieldProps('squareFootage', 'Square footage')} />
        </Grid>
        <Grid size={{ xs: 12 }}>
          <ImagePicker
            images={values.images}
            onChange={(images) => setValues((prev) => ({ ...prev, images }))}
          />
          <Typography variant="caption" color="text.secondary">
            The address is verified automatically against OpenStreetMap when the property is created.
          </Typography>
        </Grid>
        <Grid size={{ xs: 12 }}>
          <Stack direction="row" spacing={2}>
            <Button type="submit" variant="contained" disabled={submitting}>
              {submitting ? <CircularProgress size={20} color="inherit" /> : submitLabel}
            </Button>
          </Stack>
        </Grid>
      </Grid>
    </Box>
  )
}
