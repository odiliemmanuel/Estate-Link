import { useCallback, useEffect, useState } from 'react'
import Button from '@mui/material/Button'
import Grid from '@mui/material/Grid'
import AddIcon from '@mui/icons-material/Add'
import { Link } from 'react-router-dom'
import { propertyService } from '../services/property.service'
import { PropertyCard } from '../components/property/PropertyCard'
import { PageHeader } from '../components/ui/PageHeader'
import { EmptyState, ErrorState, LoadingState } from '../components/ui/StateViews'
import { extractErrorMessage } from '../lib/error'
import type { Property } from '../types/property'

export function MyPropertiesPage({ title = 'My properties' }: { title?: string }) {
  const [properties, setProperties] = useState<Property[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    setError(null)
    setProperties(null)
    propertyService
      .getMine()
      .then(setProperties)
      .catch((err: unknown) => setError(extractErrorMessage(err)))
  }, [])

  useEffect(load, [load])

  return (
    <>
      <PageHeader
        title={title}
        subtitle="Properties you own or manage."
        action={
          <Button component={Link} to="/properties/new" variant="contained" startIcon={<AddIcon />}>
            Add property
          </Button>
        }
      />
      {error ? (
        <ErrorState message={error} onRetry={load} />
      ) : !properties ? (
        <LoadingState label="Loading your properties…" />
      ) : properties.length === 0 ? (
        <EmptyState
          title="No properties yet"
          message="Add your first property to start creating listings."
          action={
            <Button component={Link} to="/properties/new" variant="contained" startIcon={<AddIcon />}>
              Add property
            </Button>
          }
        />
      ) : (
        <Grid container spacing={3}>
          {properties.map((property) => (
            <Grid key={property.id} size={{ xs: 12, sm: 6, md: 4 }}>
              <PropertyCard property={property} />
            </Grid>
          ))}
        </Grid>
      )}
    </>
  )
}
