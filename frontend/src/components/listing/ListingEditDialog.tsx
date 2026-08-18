import { useEffect, useState } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { listingService } from '../../services/listing.service'
import { propertyService } from '../../services/property.service'
import { ImagePicker } from '../property/ImagePicker'
import { useToast } from '../../context/ToastContext'
import { extractErrorMessage } from '../../lib/error'
import type { Listing, Property } from '../../types/property'

interface ListingEditDialogProps {
  open: boolean
  listing: Listing | null
  property?: Property
  onClose: () => void
  onSaved: (listing: Listing, property?: Property) => void
}

// Admin-only edit of a listing's price/title/description plus the property
// photos shown on the card. Both PATCHes are enforced server-side (ADMIN for
// listings; OWNER/AGENT/ADMIN for property images).
export function ListingEditDialog({ open, listing, property, onClose, onSaved }: ListingEditDialogProps) {
  const toast = useToast()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [images, setImages] = useState<string[]>([])
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (listing) {
      setTitle(listing.title)
      setDescription(listing.description ?? '')
      setPrice(String(listing.price))
    }
    setImages(property?.imageUrls ?? [])
  }, [listing, property, open])

  const handleSave = async () => {
    if (!listing) return
    const parsedPrice = Number(price)
    if (!Number.isFinite(parsedPrice) || parsedPrice < 0) {
      toast.error('Enter a valid price')
      return
    }
    setSaving(true)
    try {
      const updated = await listingService.update(listing.id, {
        title,
        description,
        price: parsedPrice,
      })

      let updatedProperty = property
      if (property) {
        const nextImages = images
        const changed = nextImages.join('\n') !== (property.imageUrls ?? []).join('\n')
        if (changed) {
          updatedProperty = await propertyService.updateImages(property.id, nextImages)
        }
      }

      toast.success('Listing updated')
      onSaved(updated, updatedProperty)
      onClose()
    } catch (err) {
      toast.error(extractErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle sx={{ fontFamily: '"Playfair Display", Georgia, serif', fontWeight: 700 }}>
        Edit listing
      </DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2} sx={{ pt: 1 }}>
          <TextField
            label="Title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            fullWidth
          />
          <TextField
            label="Description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            fullWidth
            multiline
            minRows={3}
          />
          <TextField
            label="Price (₦)"
            type="number"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            fullWidth
          />
          <Box>
            <ImagePicker images={images} onChange={setImages} />
            <Typography variant="caption" color="text.secondary">
              These photos are shown on the listing card.
            </Typography>
          </Box>
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={saving}>
          Cancel
        </Button>
        <Button onClick={handleSave} variant="contained" color="secondary" disabled={saving}>
          {saving ? 'Saving…' : 'Save changes'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
