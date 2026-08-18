# EstateLink — Product Requirements Document

## 1. Overview

EstateLink is a property management platform connecting **property owners**, **agents**, **prospective tenants/buyers (applicants)**, and **admins**. Owners list properties; agents manage listings and schedule inspections; applicants browse listings and submit expressions of interest; admins gate the marketplace by approving listings and managing agent accounts.

The system is built as six cooperating Spring Boot 3.3 microservices, Java 21, connected by Kafka for asynchronous events and a shared JWT secret for stateless authentication. Each service owns its own PostgreSQL database (database-per-service pattern).

---

## 2. Actors

| Actor | Description |
|---|---|
| **Owner** | A property owner who registers on the platform, creates properties, assigns agents, and manages listings through them. |
| **Agent** | A real-estate professional who manages listings on behalf of one or more owners. Agents schedule inspections, send offers, and are notified when inspection requests arrive. |
| **Applicant** | A prospective tenant or buyer. Browses approved listings, requests property inspections, and submits rental/purchase expressions of interest. |
| **Admin** | Platform administrator. Approves or rejects new listings, manages agent activation/suspension, and views platform analytics. |

---

## 3. Core Entities

| Entity | Owner | Description |
|---|---|---|
| `User` | user-service | Any registered person. Holds role, status, and email-verification token. |
| `Property` | property-service | A physical property with address, type, availability, and media. Address is geocoded via OpenStreetMap Nominatim at creation time. |
| `Listing` | property-service | A market-facing publication of a property (for rent or sale). Starts as `PENDING_APPROVAL` and becomes `ACTIVE` only after admin approval. |
| `InspectionRequest` | inspection-service | A request by an applicant to visit a listed property on a particular date/time. |
| `InspectionSlot` | inspection-service | An available time window published by the agent for a listing. An inspection request is matched to one of these. |
| `Offer` | offer-service | A formal expression of interest (rent or purchase) sent by an agent on behalf of an applicant. |

### Supporting enumerations

| Enum | Values |
|---|---|
| `Role` | `OWNER`, `AGENT`, `APPLICANT`, `ADMIN` |
| `UserStatus` | `UNVERIFIED`, `ACTIVE`, `SUSPENDED` |
| `PropertyType` | `RESIDENTIAL`, `COMMERCIAL`, `INDUSTRIAL`, `RETAIL`, `SPECIAL_PURPOSE` |
| `AvailabilityStatus` | `AVAILABLE`, `LEASED`, `SOLD`, `UNAVAILABLE`, `UNDER_MAINTENANCE` |
| `Purpose` | `FOR_RENT`, `FOR_SALE` |
| `ListingStatus` | `DRAFT`, `PENDING_APPROVAL`, `ACTIVE`, `REJECTED`, `UNDER_OFFER`, `SUSPENDED`, `CLOSED` |

---

## 4. Service Boundaries and Responsibilities

### 4.1 user-service (port 8081)

Owns the `users` table and the authentication lifecycle.

**Endpoints:**
| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | public | Register; user starts as UNVERIFIED. Publishes `user.registered` to Kafka. |
| POST | `/api/v1/auth/login` | public | Returns JWT only if user is ACTIVE. |
| GET | `/api/v1/auth/verify?token=` | public | Sets user to ACTIVE; clears token (single-use, 24 h expiry). |
| GET | `/api/v1/users/{id}` | authenticated | Profile retrieval. |
| PUT | `/api/v1/users/{id}` | authenticated | Update name / email / password. |
| GET | `/api/v1/users/agents` | ADMIN | All agents. |
| GET | `/api/v1/users/agents/pending` | ADMIN | Agents with UNVERIFIED status. |
| GET | `/api/v1/users/status/{status}` | ADMIN | Users filtered by status. |
| PUT | `/api/v1/users/{id}/status?status=` | ADMIN | Activate or suspend a user. |

**Events produced:** `user.registered`

---

### 4.2 property-service (port 8082)

Owns the `properties` and `listings` tables.

**Property endpoints:**
| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/v1/properties` | OWNER, AGENT | Create property; address geocoded against Nominatim. |
| GET | `/api/v1/properties/{id}` | authenticated | Property detail (includes geocoded coordinates and formatted address). |
| GET | `/api/v1/properties/my` | OWNER, AGENT | Properties owned or assigned to the requesting user. |
| PATCH | `/api/v1/properties/{id}/agent` | OWNER | Assign an agent to a property. |
| PATCH | `/api/v1/properties/{id}/status?availabilityStatus=` | OWNER, ADMIN | Change availability (AVAILABLE, LEASED, SOLD, UNAVAILABLE, UNDER_MAINTENANCE). |
| PATCH | `/api/v1/properties/{id}/images` | OWNER, AGENT | Replace the image URL list. |

**Listing endpoints:**
| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/v1/listings` | OWNER, AGENT | Create listing for a property. Status starts at PENDING_APPROVAL. |
| GET | `/api/v1/listings` | public | All ACTIVE listings (marketplace browse). |
| GET | `/api/v1/listings/pending` | ADMIN | Listings awaiting approval. |
| GET | `/api/v1/listings/property/{propertyId}` | public | Listings for a given property. |
| PATCH | `/api/v1/listings/{id}/approve` | ADMIN | Sets ACTIVE; publishes `listing.approved` to Kafka. |
| PATCH | `/api/v1/listings/{id}/reject` | ADMIN | Sets REJECTED. |
| PATCH | `/api/v1/listings/{id}/suspend` | ADMIN, AGENT | Sets SUSPENDED. |

**Events produced:** `listing.approved`

---

### 4.3 inspection-service (port 8083) — *implemented*

Owns the `inspection_requests` and `inspection_slots` tables.

**Responsibilities:**
- Agents publish inspection slots (available time windows) for a listing.
- Applicants submit inspection requests for a listing.
- The service enforces **no double-booking**: slot creation rejects overlapping windows for the same listing (service-level check plus a unique constraint on `(listing_id, slot_start)` as a backstop), and booking a slot takes a pessimistic write lock so two applicants can never grab the same slot concurrently.
- When an inspection request is submitted, the service publishes `inspection.requested` to Kafka (keyed by agent id).
- Agents accept or decline requests; declining (or the applicant cancelling) releases the slot back to `OPEN`.
- Applicants can cancel before the inspection date.

**Events produced:** `inspection.requested`
**Events consumed:** `listing.approved` (to initialise slots for a newly active listing) *(not yet wired)*

---

### 4.4 offer-service (port 8084) — *planned*

Owns the `offers` table.

**Responsibilities:**
- Agents send offers (rental terms or purchase price) on behalf of applicants for a listing.
- Listing status transitions to UNDER_OFFER when the first offer is sent.
- Owners (through agents) or applicants can accept or reject offers.
- Accepting an offer transitions listing status to CLOSED and sets property availability accordingly.

**Events produced:** `offer.accepted`, `offer.rejected`
**Events consumed:** `listing.approved` (to link offers to active listings)

---

### 4.5 notification-service (port 8085) — *partially implemented*

Stateless Kafka consumer + email sender. No database, no controllers.

**Events consumed:**
| Topic | Action |
|---|---|
| `user.registered` | Sends a verification email with a link to `{FRONTEND}/?token={token}`. |
| `listing.approved` | Sends an approval notification email to the property owner. *(not yet wired)* |
| `inspection.requested` | Sends a notification email to the assigned agent. *(consumer wired; email delivery deferred until the service can resolve the agent's address)* |

**Technology:** Jakarta Mail via Gmail SMTP (STARTTLS, port 587).

---

### 4.6 analytics-service (port 8086) — *planned*

Owns an `analytics_snapshots` table (materialised aggregations).

**Responsibilities:**
- Consumes domain events (`listing.approved`, `offer.accepted`, `inspection.requested`) and maintains aggregated counters (listings by status, inspections by period, agent performance metrics).
- Exposes read-only dashboard endpoints for admins: daily/weekly/monthly KPIs.
- A scheduled task (Spring `@Scheduled`) refreshes the snapshots periodically.

**Events consumed:** `listing.approved`, `offer.accepted`, `inspection.requested`

---

## 5. API Gateway (planned)

Spring Cloud Gateway routes all `/api/**` traffic through a single entry point (port 9090). Responsibilities:
- JWT validation (verify token expiry + signature using the shared secret).
- Rate limiting and circuit breaking.
- Route configuration: `/api/v1/auth/**` → user-service, `/api/v1/properties/**` + `/api/v1/listings/**` → property-service, etc.
- The frontend switches to a single proxy target: `'/api': 'http://localhost:9090'`.

---

## 6. Kafka Topics and Event Flow

| Topic | Producer | Consumer(s) |
|---|---|---|
| `user.registered` | user-service | notification-service |
| `listing.approved` | property-service | notification-service, inspection-service*, offer-service*, analytics-service* |
| `inspection.requested` | inspection-service* | notification-service*, analytics-service* |
| `offer.accepted` | offer-service* | analytics-service*, property-service* (update status) |
| `offer.rejected` | offer-service* | notification-service* |

(* = planned)

---

## 7. Non-functional requirements

| Requirement | Approach |
|---|---|
| **Authentication** | Stateless JWT (HMAC-SHA256, 24 h expiry). Issued only by user-service; validated by all other services using the shared secret. |
| **Authorization** | Role-based via Spring Security `@PreAuthorize`. The API gateway (planned) will centralise this. |
| **No double-booking** | Database-level unique constraint in inspection-service prevents the same slot from being assigned to two accepted requests. |
| **Email verification** | Single-use token with 24 h TTL; email sent asynchronously via Kafka to avoid blocking registration. |
| **Address verification** | Nominatim geocoding at property creation time; graceful degradation if the API is unavailable. |
| **Frontend SPA** | Vite + React 19 + TypeScript + MUI v9. Route-level code-splitting. Auth stored in localStorage with axios interceptor. |
| **Schema management** | Hibernate `ddl-auto=update` (not recommended for production; Flyway/Liquibase recommended for a production deployment). |
| **Config** | Per-service `.env` files via `spring-dotenv`. Secrets never committed to public repos. |

---

## 8. Workflows

### 8.1 Property listing lifecycle
1. Owner registers → `user.registered` → notification-service sends verification email.
2. Owner verifies email → status becomes ACTIVE → logs in → gets JWT.
3. Owner creates a property (address geocoded) → `AvailabilityStatus = AVAILABLE`.
4. Owner creates a listing for the property → status = PENDING_APPROVAL.
5. Admin reviews the listing in Pending Listings page → approves → status = ACTIVE; `listing.approved` event published.
6. Notification-service emails the owner confirming approval.
7. Listing appears in the public Browse Listings page.

### 8.2 Inspection flow (implemented)
1. Agent publishes inspection slots for an active listing.
2. Applicant browses the listing, selects a slot, and submits an inspection request.
3. Inspection-service locks the slot (pessimistic), validates it is `OPEN`, creates the request, and publishes `inspection.requested`.
4. Notification-service consumes the event (agent email delivery deferred).
5. Agent reviews the request, accepts → the slot stays locked. Or declines → the slot becomes `OPEN` again.

### 8.3 Offer flow (planned)
1. Agent sends an offer on behalf of an applicant for a listing.
2. Listing status changes to UNDER_OFFER.
3. Owner (through the agent) or applicant accepts the offer.
4. Listing status becomes CLOSED; property availability changes to LEASED or SOLD.
5. `offer.accepted` event is published → analytics-service records the transaction.

---

## 9. Data store summary

| Service | Database | Host port | Tables |
|---|---|---|---|
| user-service | Supabase PostgreSQL (external) | 5432 | `users` |
| property-service | PostgreSQL (Docker) | 5434→5432 | `properties`, `listings`, `property_images` |
| inspection-service | PostgreSQL (Docker) | 5435→5432 | `inspection_requests`, `inspection_slots` |
| offer-service (planned) | PostgreSQL | 5436 | `offers` |
| analytics-service (planned) | PostgreSQL | 5437 | `analytics_snapshots` |
| notification-service | none | — | — |
