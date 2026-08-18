import { useRef, useState } from 'react'
import type { ChangeEvent, KeyboardEvent } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import IconButton from '@mui/material/IconButton'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import AddPhotoAlternateIcon from '@mui/icons-material/AddPhotoAlternate'
import CloseIcon from '@mui/icons-material/Close'
import LinkIcon from '@mui/icons-material/Link'
import { uploadService } from '../../services/upload.service'
import { useToast } from '../../context/ToastContext'
import { extractErrorMessage } from '../../lib/error'

interface ImagePickerProps {
  images: string[]
  onChange: (images: string[]) => void
  max?: number
}

function looksLikeUrl(value: string): boolean {
  const trimmed = value.trim()
  return /^(https?:\/\/|\/)/i.test(trimmed)
}

export function ImagePicker({ images, onChange, max = 10 }: ImagePickerProps) {
  const toast = useToast()
  const fileInputRef = useRef<HTMLInputElement | null>(null)
  const [uploading, setUploading] = useState(false)
  const [pasteUrl, setPasteUrl] = useState('')

  const handleFiles = async (event: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? [])
    event.target.value = ''
    if (files.length === 0) return

    const remaining = max - images.length
    if (remaining <= 0) {
      toast.error(`You can add up to ${max} images.`)
      return
    }

    setUploading(true)
    try {
      const accepted = files.slice(0, remaining)
      const uploaded: string[] = []
      for (const file of accepted) {
        try {
          uploaded.push(await uploadService.uploadImage(file))
        } catch (err) {
          toast.error(`"${file.name}": ${extractErrorMessage(err)}`)
        }
      }
      if (uploaded.length > 0) onChange([...images, ...uploaded])
    } finally {
      setUploading(false)
    }
  }

  const handleAddUrl = () => {
    const url = pasteUrl.trim()
    if (!url) return
    if (!looksLikeUrl(url)) {
      toast.error('Enter a full URL starting with http:// or https://')
      return
    }
    if (images.length >= max) {
      toast.error(`You can add up to ${max} images.`)
      return
    }
    onChange([...images, url])
    setPasteUrl('')
  }

  const handlePasteKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault()
      handleAddUrl()
    }
  }

  const removeImage = (index: number) => {
    onChange(images.filter((_, i) => i !== index))
  }

  return (
    <Box>
      <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', mb: 1 }}>
        {images.map((image, index) => (
          <Box key={image} sx={{ position: 'relative', width: 88, height: 88 }}>
            <Box
              component="img"
              src={image}
              alt={`Image ${index + 1}`}
              sx={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 1 }}
            />
            <Tooltip title="Remove">
              <IconButton
                size="small"
                aria-label="Remove image"
                onClick={() => removeImage(index)}
                sx={{
                  position: 'absolute',
                  top: 4,
                  right: 4,
                  bgcolor: 'rgba(0,0,0,0.55)',
                  color: '#fff',
                  p: 0.5,
                  '&:hover': { bgcolor: 'rgba(0,0,0,0.8)' },
                }}
              >
                <CloseIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Box>
        ))}
      </Stack>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ alignItems: { sm: 'center' } }}>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/png,image/jpeg,image/gif,image/webp"
          multiple
          hidden
          onChange={handleFiles}
        />
        <Button
          variant="outlined"
          startIcon={uploading ? <CircularProgress size={18} /> : <AddPhotoAlternateIcon />}
          onClick={() => fileInputRef.current?.click()}
          disabled={uploading}
        >
          {uploading ? 'Uploading…' : 'Upload images'}
        </Button>

        <TextField
          size="small"
          fullWidth
          placeholder="…or paste an image URL"
          value={pasteUrl}
          onChange={(e) => setPasteUrl(e.target.value)}
          onKeyDown={handlePasteKeyDown}
          slotProps={{ input: { startAdornment: <LinkIcon fontSize="small" sx={{ mr: 1, opacity: 0.6 }} /> } }}
        />
        <Button variant="contained" color="secondary" onClick={handleAddUrl} disabled={!pasteUrl.trim()}>
          Add
        </Button>
      </Stack>

      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
        PNG, JPG, GIF or WEBP, up to 5 MB each (max {max} images).
      </Typography>
    </Box>
  )
}
