import { apiClient } from '../lib/apiClient'
import { SERVICE_ROUTES } from '../config/api'

const base = SERVICE_ROUTES.property.uploads

// property-service: POST /api/v1/uploads (multipart). Returns a relative URL
// like /api/v1/uploads/<uuid>.png that is served back as a static resource.
export const uploadService = {
  async uploadImage(file: File): Promise<string> {
    const form = new FormData()
    form.append('file', file)
    const response = await apiClient.post<{ url: string }>(base, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data.url
  },
}
