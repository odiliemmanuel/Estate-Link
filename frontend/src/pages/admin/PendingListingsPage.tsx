import { useCallback, useEffect, useState } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import CheckIcon from '@mui/icons-material/Check'
import CloseIcon from '@mui/icons-material/Close'
import { listingService } from '../../services/listing.service'
import { PageHeader } from '../../components/ui/PageHeader'
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateViews'
import { ListingStatusChip, PurposeChip } from '../../components/ui/StatusChip'
import { useToast } from '../../context/ToastContext'
import { extractErrorMessage } from '../../lib/error'
import { formatDate, formatPrice } from '../../lib/format'
import type { Listing } from '../../types/property'

export function PendingListingsPage() {
  const toast = useToast()
  const [listings, setListings] = useState<Listing[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<string | null>(null)

  const load = useCallback(() => {
    setError(null)
    setListings(null)
    listingService
      .getPending()
      .then(setListings)
      .catch((err: unknown) => setError(extractErrorMessage(err)))
  }, [])

  useEffect(load, [load])

  const decide = async (listing: Listing, action: 'approve' | 'reject') => {
    setBusyId(listing.id)
    try {
      if (action === 'approve') {
        await listingService.approve(listing.id)
        toast.success('Listing approved and published.')
      } else {
        await listingService.reject(listing.id)
        toast.info('Listing rejected.')
      }
      setListings((prev) => (prev ? prev.filter((item) => item.id !== listing.id) : prev))
    } catch (err) {
      toast.error(extractErrorMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <>
      <PageHeader title="Pending approvals" subtitle="Review listings submitted by owners and agents." />
      {error ? (
        <ErrorState message={error} onRetry={load} />
      ) : !listings ? (
        <LoadingState label="Loading pending listings…" />
      ) : listings.length === 0 ? (
        <EmptyState title="Nothing to review" message="All submitted listings have been handled." />
      ) : (
        <Stack spacing={2}>
          {listings.map((listing) => (
            <Card key={listing.id}>
              <CardContent>
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ justifyContent: 'space-between' }}>
                  <Box sx={{ minWidth: 0 }}>
                    <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                        {listing.title}
                      </Typography>
                      <ListingStatusChip status={listing.status} />
                      <PurposeChip purpose={listing.purpose} />
                    </Stack>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                      {listing.description}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                      Property {listing.propertyId} · Submitted {formatDate(listing.createdAt)}
                    </Typography>
                  </Box>
                  <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
                    <Typography variant="h6">{formatPrice(listing.price)}</Typography>
                    <Button
                      variant="contained"
                      color="success"
                      startIcon={<CheckIcon />}
                      disabled={busyId === listing.id}
                      onClick={() => decide(listing, 'approve')}
                    >
                      Approve
                    </Button>
                    <Button
                      variant="outlined"
                      color="error"
                      startIcon={<CloseIcon />}
                      disabled={busyId === listing.id}
                      onClick={() => decide(listing, 'reject')}
                    >
                      Reject
                    </Button>
                  </Stack>
                </Stack>
              </CardContent>
            </Card>
          ))}
        </Stack>
      )}
    </>
  )
}
