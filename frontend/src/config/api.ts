// Single source of truth for backend addressing.
//
// The frontend talks to same-origin `/api/*` paths. In development, Vite's
// server.proxy forwards each prefix to the correct microservice (see
// vite.config.ts). Set VITE_API_BASE_URL to a full origin (e.g. an
// api-gateway: `http://localhost:9090`) to bypass the proxy in production.
//
// As you implement more services, add their route prefixes to vite.config.ts
// and import the matching service module in `src/services/`.
export const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

export const SERVICE_ROUTES = {
  user: {
    auth: '/api/v1/auth',
    users: '/api/v1/users',
  },
  property: {
    properties: '/api/v1/properties',
    listings: '/api/v1/listings',
    uploads: '/api/v1/uploads',
  },
  inspection: {
    slots: '/api/v1/inspection-slots',
    requests: '/api/v1/inspection-requests',
  },
  offer: {
    offers: '/api/v1/offers',
  },
  analytics: {
    analytics: '/api/v1/analytics',
  },
} as const
