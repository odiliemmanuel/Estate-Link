# EstateLink — Class Diagram

> Render this file in any Markdown viewer that supports Mermaid (VS Code, GitHub, IntelliJ, etc.).

```mermaid
classDiagram
    direction TB

    %% ─── user-service ────────────────────────────────────────

    class User {
        <<Entity>>
        UUID id
        String name
        String email
        String password
        Role role
        UserStatus status
        LocalDateTime createdAt
        LocalDateTime updatedAt
        String verificationToken
        LocalDateTime tokenExpiresAt
    }

    class Role {
        <<Enum>>
        OWNER
        AGENT
        APPLICANT
        ADMIN
    }

    class UserStatus {
        <<Enum>>
        UNVERIFIED
        ACTIVE
        SUSPENDED
    }

    User --> Role
    User --> UserStatus

    %% ─── property-service ────────────────────────────────────

    class Property {
        <<Entity>>
        UUID id
        UUID ownerId
        UUID agentId
        String title
        String description
        String address
        String city
        String state
        Double latitude
        Double longitude
        String formattedAddress
        Boolean addressVerified
        PropertyType propertyType
        AvailabilityStatus availabilityStatus
        BigDecimal price
        Integer bedrooms
        Integer bathrooms
        Integer squareFootage
        List~String~ imageUrls
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    class PropertyType {
        <<Enum>>
        RESIDENTIAL
        COMMERCIAL
        INDUSTRIAL
        RETAIL
        SPECIAL_PURPOSE
    }

    class AvailabilityStatus {
        <<Enum>>
        AVAILABLE
        LEASED
        SOLD
        UNAVAILABLE
        UNDER_MAINTENANCE
    }

    Property --> PropertyType
    Property --> AvailabilityStatus

    class Listing {
        <<Entity>>
        UUID id
        UUID propertyId
        UUID ownerId
        UUID agentId
        UUID approvedBy
        String title
        String description
        BigDecimal price
        Purpose purpose
        ListingStatus status
        Boolean approved
        LocalDateTime approvedAt
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    class Purpose {
        <<Enum>>
        FOR_RENT
        FOR_SALE
    }

    class ListingStatus {
        <<Enum>>
        DRAFT
        PENDING_APPROVAL
        ACTIVE
        REJECTED
        UNDER_OFFER
        SUSPENDED
        CLOSED
    }

    Listing --> Purpose
    Listing --> ListingStatus

    %% ─── inspection-service ──────────────────────────────────

    class InspectionSlot {
        <<Entity>>
        UUID id
        UUID listingId
        UUID agentId
        LocalDateTime slotStart
        LocalDateTime slotEnd
        SlotStatus status
        LocalDateTime createdAt
    }

    class SlotStatus {
        <<Enum>>
        OPEN
        BOOKED
        CANCELLED
    }

    InspectionSlot --> SlotStatus

    class InspectionRequest {
        <<Entity>>
        UUID id
        UUID applicantId
        UUID listingId
        UUID slotId
        InspectionStatus status
        String message
        LocalDateTime createdAt
    }

    class InspectionStatus {
        <<Enum>>
        PENDING
        ACCEPTED
        DECLINED
        CANCELLED
        COMPLETED
    }

    InspectionRequest --> InspectionStatus

    %% ─── offer-service ───────────────────────────────────────

    class Offer {
        <<Entity>>
        UUID id
        UUID listingId
        UUID applicantId
        UUID agentId
        BigDecimal amount
        OfferType type
        OfferStatus status
        String note
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    class OfferType {
        <<Enum>>
        RENTAL
        PURCHASE
    }

    class OfferStatus {
        <<Enum>>
        SENT
        ACCEPTED
        REJECTED
        WITHDRAWN
    }

    Offer --> OfferType
    Offer --> OfferStatus

    %% ─── Relationships ───────────────────────────────────────

    User "1" --> "*" Property : owns (ownerId)
    User "1" --> "*" Property : manages (agentId)
    User "1" --> "*" Listing : owns (ownerId)
    User "1" --> "*" Listing : manages (agentId)
    User "1" --> "*" Listing : approves (approvedBy)
    Property "1" --> "*" Listing

    User "1" --> "*" InspectionRequest : submits (applicantId)
    User "1" --> "*" InspectionSlot : publishes (agentId)
    Listing "1" --> "*" InspectionSlot
    Listing "1" --> "*" InspectionRequest
    InspectionSlot "1" --> "0..1" InspectionRequest : assigned to

    User "1" --> "*" Offer : sends (agentId)
    User "1" --> "*" Offer : relates to (applicantId)
    Listing "1" --> "*" Offer
```

---

### Plain-text description (for non-Mermaid contexts)

```
User
 ├── owns → Property[] (ownerId)
 ├── manages → Property[] (agentId)
 ├── owns → Listing[] (ownerId)
 ├── manages → Listing[] (agentId)
 ├── approves → Listing[] (approvedBy)
 ├── submits → InspectionRequest[] (applicantId)
 ├── publishes → InspectionSlot[] (agentId)
 ├── sends → Offer[] (agentId)
 └── relates to → Offer[] (applicantId)

Property
 ├── type → PropertyType
 ├── availability → AvailabilityStatus
 └── listings → Listing[]

Listing
 ├── property → Property
 ├── purpose → Purpose
 ├── status → ListingStatus
 ├── slots → InspectionSlot[]
 ├── requests → InspectionRequest[]
 └── offers → Offer[]

InspectionSlot
 ├── listing → Listing
 ├── agent → User
 └── assigned request → InspectionRequest (0..1)

InspectionRequest
 ├── applicant → User
 ├── listing → Listing
 └── slot → InspectionSlot

Offer
 ├── listing → Listing
 ├── agent → User
 └── applicant → User
```
