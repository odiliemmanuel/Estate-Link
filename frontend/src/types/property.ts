// Mirrors property-service enums: com.estatelink.property.domain
export type PropertyType = 'RESIDENTIAL' | 'COMMERCIAL' | 'INDUSTRIAL' | 'RETAIL' | 'SPECIAL_PURPOSE'
export type AvailabilityStatus = 'AVAILABLE' | 'LEASED' | 'SOLD' | 'UNAVAILABLE' | 'UNDER_MAINTENANCE'
export type ListingStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'ACTIVE' | 'REJECTED' | 'UNDER_OFFER' | 'SUSPENDED' | 'CLOSED'
export type Purpose = 'FOR_RENT' | 'FOR_SALE'

// Matches dto/responses/PropertyResponse
export interface Property {
  id: string
  ownerId: string
  agentId: string | null
  title: string
  description?: string
  address: string
  city: string
  state: string
  formattedAddress?: string
  latitude?: number | null
  longitude?: number | null
  addressVerified: boolean
  propertyType: PropertyType
  availabilityStatus: AvailabilityStatus
  price: number
  bedrooms?: number
  bathrooms?: number
  squareFootage?: number
  imageUrls: string[]
  createdAt: string
}

// Matches dto/requests/CreatePropertyRequest
export interface CreatePropertyRequest {
  title: string
  description?: string
  address: string
  city: string
  state: string
  propertyType: PropertyType
  price: number
  bedrooms?: number
  bathrooms?: number
  squareFootage?: number
  imageUrls?: string[]
}

// Matches dto/responses/ListingResponse
export interface Listing {
  id: string
  propertyId: string
  ownerId: string
  agentId: string | null
  title: string
  description?: string
  price: number
  purpose: Purpose
  status: ListingStatus
  approved: boolean
  approvedAt?: string | null
  createdAt: string
}

// Matches dto/requests/CreateListingRequest
export interface CreateListingRequest {
  propertyId: string
  title: string
  description?: string
  price: number
  purpose: Purpose
}

// Matches dto/requests/UpdateListingRequest (admin edits)
export interface UpdateListingRequest {
  title: string
  description?: string
  price: number
}

// Matches dto/requests/AssignAgentRequest
export interface AssignAgentRequest {
  agentId: string
}
