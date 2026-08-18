import { apiClient } from '../lib/apiClient'
import { SERVICE_ROUTES } from '../config/api'
import type { CreateListingRequest, Listing, UpdateListingRequest } from '../types/property'

const base = SERVICE_ROUTES.property

// property-service: /api/v1/listings
export const listingService = {
  // OWNER/AGENT only.
  create(payload: CreateListingRequest): Promise<Listing> {
    return apiClient.post<Listing>(`${base.listings}`, payload).then((r) => r.data)
  },

  // Public — only ACTIVE listings.
  getActive(): Promise<Listing[]> {
    return apiClient.get<Listing[]>(`${base.listings}`).then((r) => r.data)
  },

  // Admin only.
  getPending(): Promise<Listing[]> {
    return apiClient.get<Listing[]>(`${base.listings}/pending`).then((r) => r.data)
  },

  getByProperty(propertyId: string): Promise<Listing[]> {
    return apiClient
      .get<Listing[]>(`${base.listings}/property/${propertyId}`)
      .then((r) => r.data)
  },

  approve(id: string): Promise<Listing> {
    return apiClient.patch<Listing>(`${base.listings}/${id}/approve`).then((r) => r.data)
  },

  // Admin only — edits title, description and price.
  update(id: string, payload: UpdateListingRequest): Promise<Listing> {
    return apiClient.patch<Listing>(`${base.listings}/${id}`, payload).then((r) => r.data)
  },

  reject(id: string): Promise<Listing> {
    return apiClient.patch<Listing>(`${base.listings}/${id}/reject`).then((r) => r.data)
  },

  suspend(id: string): Promise<Listing> {
    return apiClient.patch<Listing>(`${base.listings}/${id}/suspend`).then((r) => r.data)
  },
}
