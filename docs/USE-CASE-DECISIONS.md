# EstateLink — Use-Case Analysis, Design Decisions & Trade-offs

---

## 1. Use-Case Summary

### Owner
| ID | Use Case | Status |
|---|---|---|
| UC-01 | Register as owner | Done |
| UC-02 | Verify email | Done |
| UC-03 | Login / logout | Done |
| UC-04 | Create a property (address auto-verified) | Done |
| UC-05 | Edit property details / images | Done |
| UC-06 | Assign an agent to a property | Done |
| UC-07 | Change property availability status | Done |
| UC-08 | Create a listing (starts pending) | Done |
| UC-09 | View own properties and their listings | Done |
| UC-10 | View listing status (pending / approved / rejected) | Done |
| UC-11 | Accept or decline an offer (via agent) | Planned |

### Agent
| ID | Use Case | Status |
|---|---|---|
| UC-12 | Register as agent (starts unverified) | Done |
| UC-13 | Manage properties assigned to self | Done |
| UC-14 | Create listings for managed properties | Done |
| UC-15 | Suspend a listing they manage | Done |
| UC-16 | Publish inspection slots for a listing | Done |
| UC-17 | Receive notification when inspection is requested | Partial (consumer wired; email deferred) |
| UC-18 | Accept or decline an inspection request | Done |
| UC-19 | Send an offer on behalf of an applicant | Planned |
| UC-20 | View inspection schedule | Done (GET /my) |

### Applicant
| ID | Use Case | Status |
|---|---|---|
| UC-21 | Register as applicant | Done |
| UC-22 | Browse approved listings | Done |
| UC-23 | Request a property inspection | Done |
| UC-24 | Cancel an inspection request | Done |
| UC-25 | Submit a rental/purchase expression of interest | Planned |
| UC-26 | View status of own requests and offers | Planned |

### Admin
| ID | Use Case | Status |
|---|---|---|
| UC-27 | Review and approve / reject listings | Done |
| UC-28 | View all agents and pending agents | Done |
| UC-29 | Activate or suspend an agent account | Done |
| UC-30 | Change property availability status | Done |
| UC-31 | View platform analytics dashboard | Planned |

---

## 2. Architectural Decisions

### AD-01 — Microservices over monolith

**Decision:** Split the system into six bounded-context services communicating via Kafka.

**Rationale:**
- The brief explicitly requires microservices.
- Each service owns one database table cluster with a clear responsibility boundary.
- Kafka decouples notification/analytics from core domain logic — a failing notification-service never blocks a property listing.

**Trade-off:**
- Network latency between services, complexity of distributed transactions (listing approval involves property-service, notification-service, and analytics-service).
- Operational overhead of running six JVM processes and six databases.
- For a student project this demonstrates architectural competence; for a production system with a small team, a modular monolith would be simpler and sufficient to start.

---

### AD-02 — Kafka for event-driven communication

**Decision:** Domain events (`user.registered`, `listing.approved`, `inspection.requested`, `offer.accepted`) travel through Kafka.

**Rationale:**
- The brief specifies Kafka as a natural trigger: "InspectionRequest submitted notifies assigned agent."
- Producers fire-and-forget; consumers process independently. A consumer outage doesn't affect the producing service.
- Enables easy addition of analytics-service later without touching any existing producer.

**Trade-off:**
- Eventual consistency: the notification email arrives seconds after registration, not synchronously. The user sees "account created — check your email" while verification may not have sent yet if Kafka is slow.
- Duplicated event DTOs per service (each service redefines `UserRegisteredEvent`). A shared library avoids this but couples services at the class level — the current approach trades DRY for independent deployability. This is documented as a deliberate choice.

---

### AD-03 — Shared JWT secret (symmetric HMAC-SHA256)

**Decision:** user-service issues tokens; property-service validates them using the same `JWT_SECRET` value injected from `.env`.

**Rationale:**
- Simplest approach when an API gateway is not yet in place. Each service independently validates the token and extracts `userId` and `role` from its claims.
- No service-to-service HTTP calls are required for auth.

**Trade-off:**
- Every service must hold the same secret. If the secret leaks, any service can be impersonated. The API gateway (planned) centralises validation so downstream services never see the secret.
- Token revocation is not possible without a token blacklist (not implemented). Users stay logged in for 24 h even after password change.
- Alternatives considered: OAuth2 Authorization Code with a dedicated IdP (e.g. Keycloak) — more standards-compliant but heavy for this project scope.

---

### AD-04 — Database-per-service

**Decision:** Each service owns its PostgreSQL database; no service reads another's tables.

**Rationale:**
- Enforces loose coupling: property-service has no JPA relationship to the `users` table. It receives `ownerId` as a UUID and never joins across databases.
- Enables independent schema evolution (e.g. inspection-service can add tables without coordinating with property-service).

**Trade-off:**
- Cross-service queries require either REST calls or eventual consistency. The `ListingResponse` DTO contains `ownerId` and `agentId` but not the owner's name or email — the frontend must call user-service separately if it needs that information.
- user-service connects to Supabase (cloud) while property-service runs a local Docker PostgreSQL, highlighting a real-world hybrid deployment but also an inconsistency in provisioning.

---

### AD-05 — `ddl-auto=update` (Hibernate) for schema management

**Decision:** Schema is created/updated automatically by Hibernate.

**Rationale:**
- Fastest iteration speed during development — new columns added in `@Entity` classes appear immediately after restart.
- Appropriate for a demonstration project where the schema is not yet stable.

**Trade-off:**
- Not safe for production: Hibernate may drop columns, lose data, or create incorrect indexes. Flyway or Liquibase is the standard replacement for production use. This trade-off is explicitly acknowledged.

---

### AD-06 — No API gateway yet

**Decision:** Frontend and services communicate directly; API gateway is deferred.

**Rationale:**
- Enables parallel development of each service without waiting for gateway routing configuration.
- The frontend uses Vite dev-server proxy to route `/api/v1/*` paths to the correct backend port, avoiding CORS entirely.

**Trade-off:**
- JWT validation is duplicated in every service. A misconfigured service could accept expired tokens.
- No centralised rate limiting, request logging, or circuit breaking.
- When implemented, the gateway simplifies the frontend to a single base URL and unifies auth.

---

### AD-07 — Event DTOs duplicated per service

**Decision:** Each consuming service redefines its own copy of `UserRegisteredEvent` / `ListingApprovedEvent` rather than sharing a common library.

**Rationale:**
- The `common` module exists as a placeholder but is commented out and empty.
- Duplicating DTOs allows each service to evolve its event shape independently. A breaking change in user-service's `UserRegisteredEvent` won't cause a compile error in notification-service.
- For a production system, a schema registry (Confluent) or Avro serialization would be preferred.

**Trade-off:**
- Fields must be kept in sync manually (e.g. `ownerEmail` was added to `ListingApprovedEvent` in property-service but the notification-service copy still lacks it).
- Any field addition requires updating both copies.

---

### AD-08 — Geocoding via Nominatim (OpenStreetMap)

**Decision:** Addresses are geocoded at property-creation time using the free Nominatim API.

**Rationale:**
- Zero cost; no API key required.
- Sufficient accuracy for a demo (latitude, longitude, formatted address).

**Trade-off:**
- Rate limits (1 req/s per IP). If a batch property import is needed in the future, Google Maps or Mapbox would be required.
- Graceful degradation: if Nominatim is unreachable, the property is created with `addressVerified = false` and no coordinates, rather than failing.

---

## 3. Anti-corruption boundaries

| From | To | Mechanism | Example |
|---|---|---|---|
| user-service → all | event payload | User's email is not embedded in property-service events | `ListingApprovedEvent.ownerEmail` is currently `null` until a REST call or enriched event is implemented |
| property-service ↔ user-service | REST (future) / event | property-service never queries the `users` table directly | It stores `ownerId` / `agentId` as opaque UUIDs |
| inspection-service ↔ property-service | Kafka event | inspection-service reads `listing.approved` to know which listings accept inspections | Never queries property-service's database |
| frontend ↔ backend | HTTP + JWT | Frontend is decoupled from service topology | Vite proxy routes; changing service ports only affects `vite.config.ts` |

---

## 4. Security design decisions

| Decision | Notes |
|---|---|
| BCrypt password hashing | Spring Security default; work factor sufficient for demo |
| Stateless sessions (no server-side session) | Required for stateless microservices; each request carries the JWT |
| CORS `allowedOriginPatterns = "*"` | Permitted only during development; must be restricted before production |
| `.env` files for secrets | Prevents hardcoding; but `user-service/.env` and `property-service/.env` are currently tracked in git — must be fixed before any public exposure |
| Email verification token single-use, 24 h TTL | Prevents replay attacks; token is cleared after use |
| Admin-only endpoints protected by `@PreAuthorize("hasRole('ADMIN')")` | Method-level security enforced in both user-service and property-service |

---

## 5. Frontend design decisions

| Decision | Rationale | Trade-off |
|---|---|---|
| Vite dev proxy (no CORS) | Eliminates CORS complexity in development; switching to API gateway is a one-line change | API calls in production need the gateway or CORS headers |
| Service modules are plain async functions (no React coupling) | Reusable from any future page; easy to test independently | No built-in caching or SWR; each page fetches on mount |
| Route-level code splitting (`React.lazy`) | Keeps initial bundle small (94 KB gzip main) | Adds a brief loading spinner on first navigation |
| Auth in localStorage (not httpOnly cookie) | JWT can be sent via axios interceptor without backend cookie support | Vulnerable to XSS; acceptable for a demo, not for production |
| Centralised error normalisation (`lib/error.ts`) | Handles all backend error shapes (plain string, `{error: ...}`, `{field: msg}`, empty 401) | Some edge cases (403 null body from login) may be overly broad |

---

## 6. Known limitations and future work

1. **No "my listings" endpoint** — the frontend derives this by loading all properties then fetching listings per property. An optimized endpoint would be more efficient.
2. **Agent assignment requires pasting a UUID** — `GET /users/agents` is admin-only; owners cannot discover agent IDs without admin help.
3. **`GET /listings/{id}`** is permitted in SecurityConfig but has no controller method — a 404 is returned.
4. **`ownerEmail` in `ListingApprovedEvent`** is hardcoded to `null` pending a mechanism to fetch it (REST call to user-service or enriching the event payload).
5. **Tests are mostly stubs** — user-service integration/security tests are entirely commented out. Notification-service has only a `contextLoads` test.
6. **Schema management** uses `ddl-auto=update`; should migrate to Flyway before production.
7. **No health checks or observability** — Prometheus actuator, distributed tracing (Sleuth/Micrometer) and container health checks are not yet configured.
8. **No retry or circuit breaking** on Kafka consumers or REST calls (resilience4j or similar is not yet in use).
9. **Inspection notifications are logged, not emailed** — the `inspection.requested` consumer has no way to resolve the agent's email address yet (same gap as `ownerEmail`); the event can be enriched with an address once user-service can be queried.
10. **inspection-service does not consume `listing.approved`** — there is no automatic slot initialisation when a listing becomes active; agents create slots ad hoc via the API.
