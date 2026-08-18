# EstateLink Frontend

React + TypeScript + Vite SPA for the EstateLink property management platform,
built with Material UI (MUI) v9.

It currently covers the services that are implemented in the backend:

| Backend service | Port | Frontend coverage |
| --- | --- | --- |
| **user-service** | 8081 | register, login, email verification, profile, admin agent management |
| **property-service** | 8082 | property CRUD + geocoding, listing lifecycle (create → pending → approve/reject/suspend) |
| notification-service | 8085 | (indirect) verification emails link back to `/?token=…` |
| inspection-service | _not built yet_ | reserved |
| offer-service | _not built yet_ | reserved |
| analytics-service | _not built yet_ | reserved |
| api-gateway | _not built yet_ | reserved |

## Run it

Prereqs: Node 20+ (built with Node 24), and the backend services + Kafka running.

```bash
npm install
npm run dev        # http://localhost:5173
```

The dev server proxies `/api/*` to the microservices via `vite.config.ts`.
Kafka/Zookeeper and the databases are started from the repo root with
`docker compose up -d`; each service runs with `mvn spring-boot:run` from its
own directory.

```bash
npm run build      # typecheck + production build to dist/
npm run lint       # oxlint
npm run preview    # serve the production build
```

## Architecture

The codebase is organised so each backend microservice has a corresponding
`types` module, `service` module, and (later) `pages` folder — adding a new
service never touches existing code.

```
src/
  config/api.ts              # single source of truth for backend routes/base URL
  lib/
    apiClient.ts             # axios instance: JWT injection + error normalisation
    error.ts                 # ApiError + extraction of backend error shapes
    storage.ts               # token/user persistence (localStorage)
    format.ts                # ₦ currency + date formatting
  types/                     # one file per backend service
    user.ts                  # Role, UserStatus, User, requests/responses
    property.ts              # PropertyType, AvailabilityStatus, Listing, etc.
  services/                  # one module per backend service
    auth.service.ts          # POST /auth/register|login, GET /auth/verify
    user.service.ts          # /users/* (profile + admin agent management)
    property.service.ts      # /properties/*
    listing.service.ts       # /listings/*
  context/
    AuthContext.tsx          # session state, login/logout
    ToastContext.tsx         # global snackbar toasts
  components/
    layout/                  # AppLayout (nav), ProtectedRoute, RoleGuard
    ui/                      # StatusChip, PageHeader, StateViews (loading/error/empty)
    property/                # PropertyCard, PropertyForm
    listing/                 # ListingCard, ListingForm
  pages/
    LandingPage.tsx          # public hero, handles email ?token redirect
    BrowsePage.tsx           # public active-listing catalogue
    auth/                    # Login, Register, VerifyEmail, VerifyPrompt
    DashboardPage.tsx        # role-aware stat cards + quick actions
    MyPropertiesPage.tsx     # OWNER/AGENT
    PropertyDetailPage.tsx   # detail + availability/agent actions
    CreatePropertyPage.tsx   # OWNER/AGENT
    CreateListingPage.tsx    # OWNER/AGENT
    admin/                   # PendingListingsPage, AgentsPage
    ProfilePage.tsx          # view/edit profile
```

### Key decisions

- **Same-origin API calls.** The app calls `/api/v1/...` and Vite proxies each
  prefix to the right service. No CORS in dev, and switching to the api-gateway
  later is a one-line change in `vite.config.ts`.
- **Services are plain async functions.** `src/services/*` never touches React,
  so any page/component/hook can call them — including future inspection/offer
  pages.
- **Shared JWT.** user-service issues the token; property-service validates the
  same secret. The axios interceptor attaches it to every request, so new
  services get auth for free.
- **Role-aware routing.** `RoleGuard` gates pages by `OWNER | AGENT | APPLICANT |
  ADMIN`. `authNav` in `AppLayout` drives the navbar per role.
- **Backend error shapes are normalised** in `lib/error.ts` (business errors
  `{error}`, bean-validation `{field: msg}`, plain-string bodies, empty 401/403).

## Adding a service later (e.g. inspection-service)

1. **`vite.config.ts`** — add a proxy line:
   ```ts
   '/api/v1/inspections': 'http://localhost:8083',
   ```
2. **`src/config/api.ts`** — add the route group (optional, nice for reuse).
3. **`src/types/inspection.ts`** — mirror the new backend DTOs/enums.
4. **`src/services/inspection.service.ts`** — one function per endpoint.
5. **`src/pages/...`** — new pages; wire them into `src/App.tsx` inside
   `RoleGuard`/`ProtectedRoute` and add nav entries in `AppLayout`'s `authNav`.

That's it — auth, toasts, error handling, and styling all come for free.

## Notes / known backend gaps the UI works around

- There is no "my listings" endpoint, so the UI derives an agent/owner's
  listings from their properties (`GET /properties/my` → `GET /listings/property/{id}`).
- `GET /users/agents` is admin-only, so an owner assigning an agent must paste
  the agent's user ID (shown on the admin Agents page).
- `GET /listings/{id}` is `permitAll` in SecurityConfig but has no controller
  method; the frontend therefore never calls it.
- Active-listing cards show property images only when the viewer is signed in
  (`GET /properties/{id}` is authenticated).
