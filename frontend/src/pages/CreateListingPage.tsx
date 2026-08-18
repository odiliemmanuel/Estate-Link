import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { listingService } from '../services/listing.service'
import { propertyService } from '../services/property.service'
import { ListingForm } from '../components/listing/ListingForm'
import { PageHeader } from '../components/ui/PageHeader'
import { ErrorState, LoadingState } from '../components/ui/StateViews'
import { useToast } from '../context/ToastContext'
import { extractErrorMessage } from '../lib/error'
import type { CreateListingRequest, Property } from '../types/property'

export function CreateListingPage() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const toast = useToast()
  const [properties, setProperties] = useState<Property[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const preselectId = params.get('propertyId')

  const load = () => {
    setError(null)
    setProperties(null)
    propertyService
      .getMine()
      .then(setProperties)
      .catch((err: unknown) => setError(extractErrorMessage(err)))
  }

  useEffect(load, [])

  const handleSubmit = async (payload: CreateListingRequest) => {
    try {
      const listing = await listingService.create(payload)
      toast.success('Listing created. It will go live after an admin approves it.')
      navigate(`/properties/${listing.propertyId}`, { replace: true })
    } catch (err) {
      toast.error(extractErrorMessage(err))
      throw err
    }
  }

  return (
    <>
      <PageHeader title="Create a listing" subtitle="List one of your properties for rent or sale. New listings start as pending approval." />
      {error ? (
        <ErrorState message={error} onRetry={load} />
      ) : !properties ? (
        <LoadingState label="Loading your properties…" />
      ) : properties.length === 0 ? (
        <Alert severity="info">
          You need at least one property before creating a listing.{' '}
          <a href="/properties/new">Add a property first.</a>
        </Alert>
      ) : (
        <ListingForm
          properties={properties}
          initialPropertyId={preselectId ?? undefined}
          onSubmit={handleSubmit}
        />
      )}
    </>
  )
}
