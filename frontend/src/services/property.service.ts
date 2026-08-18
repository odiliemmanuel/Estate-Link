import { apiClient } from '../lib/apiClient'
import { SERVICE_ROUTES } from '../config/api'
import type { AvailabilityStatus, CreatePropertyRequest, Property } from '../types/property'

const base = SERVICE_ROUTES.property

// property-service: /api/v1/properties
export const propertyService = {
  create(payload: CreatePropertyRequest): Promise<Property> {
    return apiClient.post<Property>(`${base.properties}`, payload).then((r) => r.data)
  },

  getById(id: string): Promise<Property> {
    return apiClient.get<Property>(`${base.properties}/${id}`).then((r) => r.data)
  },

  // OWNER/AGENT only — requires the JWT; the backend derives the user id from it.
  getMine(): Promise<Property[]> {
    return apiClient.get<Property[]>(`${base.properties}/my`).then((r) => r.data)
  },

  assignAgent(id: string, agentId: string): Promise<Property> {
    return apiClient
      .patch<Property>(`${base.properties}/${id}/agent`, { agentId })
      .then((r) => r.data)
  },

  updateStatus(id: string, availabilityStatus: AvailabilityStatus): Promise<Property> {
    return apiClient
      .patch<Property>(`${base.properties}/${id}/status`, null, { params: { availabilityStatus } })
      .then((r) => r.data)
  },

  updateImages(id: string, imageUrls: string[]): Promise<Property> {
    return apiClient
      .patch<Property>(`${base.properties}/${id}/images`, imageUrls)
      .then((r) => r.data)
  },
}
