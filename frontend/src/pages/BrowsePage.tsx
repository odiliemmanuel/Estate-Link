import { useCallback, useEffect, useMemo, useState } from 'react'
import Button from '@mui/material/Button'
import Grid from '@mui/material/Grid'
import InputAdornment from '@mui/material/InputAdornment'
import MenuItem from '@mui/material/MenuItem'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import SearchIcon from '@mui/icons-material/Search'
import { listingService } from '../services/listing.service'
import { propertyService } from '../services/property.service'
import { ListingCard } from '../components/listing/ListingCard'
import { ListingEditDialog } from '../components/listing/ListingEditDialog'
import { PageHeader } from '../components/ui/PageHeader'
import { EmptyState, ErrorState, LoadingState } from '../components/ui/StateViews'
import { useAuth } from '../context/AuthContext'
import { extractErrorMessage } from '../lib/error'
import type { Listing, Property, PropertyType, Purpose } from '../types/property'

const propertyTypes: PropertyType[] = ['RESIDENTIAL', 'COMMERCIAL', 'INDUSTRIAL', 'RETAIL', 'SPECIAL_PURPOSE']

const purposes: { value: Purpose; label: string }[] = [
  { value: 'FOR_RENT', label: 'For rent' },
  { value: 'FOR_SALE', label: 'For sale' },
]

export function BrowsePage() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'

  const [listings, setListings] = useState<Listing[] | null>(null)
  const [properties, setProperties] = useState<Record<string, Property>>({})
  const [error, setError] = useState<string | null>(null)

  // Combined filters — all active filters apply together (AND).
  const [query, setQuery] = useState('')
  const [purpose, setPurpose] = useState<'' | Purpose>('')
  const [propertyType, setPropertyType] = useState<'' | PropertyType>('')
  const [city, setCity] = useState('')
  const [maxPrice, setMaxPrice] = useState('')

  const [editing, setEditing] = useState<{ listing: Listing; property?: Property } | null>(null)

  const load = useCallback(() => {
    setError(null)
    setListings(null)
    listingService
      .getActive()
      .then(async (active) => {
        setListings(active)
        // Enrich with property details (images + location). Property lookup is
        // public, so this works for guests and signed-in users alike.
        const entries = await Promise.all(
          active.map(async (listing) => {
            try {
              const property = await propertyService.getById(listing.propertyId)
              return [listing.propertyId, property] as const
            } catch {
              return null
            }
          }),
        )
        setProperties(Object.fromEntries(entries.filter((e): e is [string, Property] => e !== null)))
      })
      .catch((err: unknown) => setError(extractErrorMessage(err)))
  }, [])

  useEffect(load, [load])

  const cities = useMemo(
    () => Array.from(new Set(Object.values(properties).map((p) => p.city).filter(Boolean))).sort(),
    [properties],
  )

  const filtered = useMemo(() => {
    if (!listings) return []
    const needle = query.trim().toLowerCase()
    const max = Number(maxPrice)

    return listings.filter((listing) => {
      const property = properties[listing.propertyId]

      const haystack = [
        listing.title,
        listing.description,
        property?.city,
        property?.state,
        property?.address,
        property?.formattedAddress,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()

      if (needle && !haystack.includes(needle)) return false
      if (purpose && listing.purpose !== purpose) return false
      if (propertyType && property && property.propertyType !== propertyType) return false
      if (city && property && property.city !== city) return false
      if (max > 0 && listing.price > max) return false
      return true
    })
  }, [listings, properties, query, purpose, propertyType, city, maxPrice])

  const hasFilters = Boolean(query || purpose || propertyType || city || maxPrice)

  const clearFilters = () => {
    setQuery('')
    setPurpose('')
    setPropertyType('')
    setCity('')
    setMaxPrice('')
  }

  const handleSaved = (updated: Listing, updatedProperty?: Property) => {
    setListings((prev) => prev?.map((l) => (l.id === updated.id ? updated : l)) ?? null)
    if (updatedProperty) {
      setProperties((prev) => ({ ...prev, [updatedProperty.id]: updatedProperty }))
    }
  }

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!listings) return <LoadingState label="Loading listings…" />

  return (
    <>
      <PageHeader
        title="Browse listings"
        subtitle="Refine by keyword, purpose, type, location or price — every filter combines."
      />

      <Paper sx={{ p: { xs: 2, md: 3 }, mb: 4 }}>
        <Stack spacing={2}>
          <TextField
            fullWidth
            placeholder="Search title, description or location…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              },
            }}
          />
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <TextField
              select
              label="Purpose"
              value={purpose}
              onChange={(e) => setPurpose(e.target.value as '' | Purpose)}
              sx={{ flex: 1 }}
            >
              <MenuItem value="">All</MenuItem>
              {purposes.map((p) => (
                <MenuItem key={p.value} value={p.value}>
                  {p.label}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Property type"
              value={propertyType}
              onChange={(e) => setPropertyType(e.target.value as '' | PropertyType)}
              sx={{ flex: 1 }}
            >
              <MenuItem value="">All</MenuItem>
              {propertyTypes.map((t) => (
                <MenuItem key={t} value={t}>
                  {t.toLowerCase().replaceAll('_', ' ')}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Location"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              sx={{ flex: 1 }}
            >
              <MenuItem value="">All</MenuItem>
              {cities.map((c) => (
                <MenuItem key={c} value={c}>
                  {c}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Max price (₦)"
              type="number"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              sx={{ flex: 1 }}
              slotProps={{ input: { inputProps: { min: 0 } } }}
            />
          </Stack>
          <Stack direction="row" spacing={2} sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="caption" color="text.secondary">
              {filtered.length} of {listings.length} listing{listings.length === 1 ? '' : 's'}
            </Typography>
            {hasFilters && (
              <Button size="small" onClick={clearFilters}>
                Clear filters
              </Button>
            )}
          </Stack>
        </Stack>
      </Paper>

      {filtered.length === 0 ? (
        <EmptyState
          title={hasFilters ? 'No listings match your filters' : 'No active listings yet'}
          message={
            hasFilters
              ? 'Try widening your search, or clear the filters to see everything.'
              : 'Check back soon — new listings appear here once an admin approves them.'
          }
        />
      ) : (
        <Grid container spacing={3}>
          {filtered.map((listing) => (
            <Grid key={listing.id} size={{ xs: 12, sm: 6, md: 4 }}>
              <ListingCard
                listing={listing}
                property={properties[listing.propertyId]}
                onEdit={isAdmin ? (l, p) => setEditing({ listing: l, property: p }) : undefined}
              />
            </Grid>
          ))}
        </Grid>
      )}

      <ListingEditDialog
        open={Boolean(editing)}
        listing={editing?.listing ?? null}
        property={editing?.property}
        onClose={() => setEditing(null)}
        onSaved={handleSaved}
      />
    </>
  )
}
