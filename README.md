# Estate-Link

A full-stack microservices-based real estate platform for listing, buying, and managing properties with agent assignment, offers, inspections, and real-time notifications.

## Tech Stack

**Backend:** Java 21, Spring Boot 3.3, Spring Cloud (Eureka), Spring Security (JWT), Apache Kafka, Spring Data JPA, PostgreSQL

**Frontend:** React 19, TypeScript, Vite, Material UI (MUI), Axios

**Infrastructure:** Docker, Docker Compose

## Architecture

Estate-Link follows a microservices architecture with event-driven communication via Kafka.

```
┌──────────────────────────────────────────────────────────────────┐
│                        Frontend (React)                          │
│                          :80 / :5173                             │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                    ┌──────▼──────┐
                    │ API Gateway │  :9090
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
   ┌─────▼─────┐   ┌──────▼──────┐   ┌──────▼──────┐
   │ User      │   │  Property   │   │  Inspection │
   │ Service   │   │  Service    │   │  Service    │
   │  :8081    │   │   :8082     │   │   :8083     │
   └───────────┘   └─────────────┘   └─────────────┘
         │                 │                 │
         │          ┌──────┴──────┐          │
         │          │             │          │
   ┌─────▼─────┐   │      ┌──────▼──────┐   │
   │ Notification   │      │   Offer     │   │
   │ Service   │   │      │   Service   │   │
   │  :8085    │   │      │    :8084    │   │
   └───────────┘   │      └─────────────┘   │
                   │                        │
             ┌─────▼─────┐                  │
             │ Analytics │                  │
             │  Service  │                  │
             │   :8086   │                  │
             └───────────┘                  │
                                            │
              ┌─────────────┐    ┌──────────▼──────┐
              │   Kafka     │◄──►│  Eureka Server  │
              └─────────────┘    │     :8761       │
                                 └─────────────────┘
```

## Microservices

| Service | Port | Description |
|---------|------|-------------|
| **Eureka Server** | 8761 | Service discovery and registration |
| **API Gateway** | 9090 | Single entry point, routing, JWT validation, rate limiting |
| **User Service** | 8081 | Authentication, registration, profile management |
| **Property Service** | 8082 | Property listings, image uploads, geocoding |
| **Inspection Service** | 8083 | Scheduling and managing property inspections |
| **Offer Service** | 8084 | Buy/rent offers with state machine transitions |
| **Notification Service** | 8085 | Email notifications via Kafka events |
| **Analytics Service** | 8086 | Listing metrics and agent performance tracking |

## Prerequisites

- Java 21
- Maven
- Node.js 18+
- Docker & Docker Compose

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/Estate-Link.git
cd Estate-Link
```

### 2. Set up environment variables

```bash
cp .env.example .env
```

Edit `.env` and fill in the values:

```env
DATABASE_USERNAME=your_db_user
DATABASE_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
MAIL_USER=your_email@gmail.com
MAIL_PASS=your_app_password
```

### 3. Run with Docker Compose

```bash
docker compose up --build
```

This starts all services, PostgreSQL, Kafka, and Zookeeper.

- **Frontend:** http://localhost
- **API Gateway:** http://localhost:9090
- **Eureka Dashboard:** http://localhost:8761

### 4. Run frontend in development mode

```bash
cd frontend
npm install
npm run dev
```

The dev server runs on http://localhost:5173 with proxy configured to each backend service.

## Project Structure

```
Estate-Link/
├── api-gateway/          # Spring Cloud Gateway
├── analytics-service/    # Listing & agent analytics
├── common/               # Shared DTOs, events, JWT utils
├── eureka-server/        # Service discovery
├── frontend/             # React + TypeScript + Vite
├── inspection-service/   # Inspection scheduling
├── notification-service/ # Email notifications via Kafka
├── offer-service/        # Buy/rent offers
├── property-service/     # Properties, listings, uploads
├── user-service/         # Auth, users, roles
├── docker-compose.yml
├── Dockerfile
└── pom.xml               # Parent Maven POM
```

## License

This project is for educational purposes.
