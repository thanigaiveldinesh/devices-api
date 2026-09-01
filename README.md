# Devices API

A production-ready REST API for managing device resources, built with Java 21 and Spring Boot 3.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Mapping | MapStruct |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers, REST Assured |
| Containerization | Docker + Docker Compose |

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

## Running the Application

### Option 1 — Docker Compose (recommended)

```bash
# Build and start all services (API + PostgreSQL)
docker compose up --build

# Run in background
docker compose up --build -d
```

The API will be available at `http://localhost:8080`.

### Option 2 — Local with external PostgreSQL

Start a PostgreSQL instance and configure environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/devicesdb
export DB_USERNAME=devices
export DB_PASSWORD=devices

mvn spring-boot:run
```

## API Documentation

Once running, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/api-docs
```

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/devices` | Create a new device |
| `GET` | `/api/v1/devices` | List all devices |
| `GET` | `/api/v1/devices?brand={brand}` | Filter by brand (case-insensitive) |
| `GET` | `/api/v1/devices?state={state}` | Filter by state |
| `GET` | `/api/v1/devices?brand={brand}&state={state}` | Filter by brand AND state combined |
| `GET` | `/api/v1/devices?page=0&size=10&sort=createdAt,desc` | Paginated and sorted |
| `GET` | `/api/v1/devices/{id}` | Get a single device |
| `PUT` | `/api/v1/devices/{id}` | Fully update a device |
| `PATCH` | `/api/v1/devices/{id}` | Partially update a device |
| `DELETE` | `/api/v1/devices/{id}` | Delete a device |

### Device States

- `AVAILABLE`
- `IN_USE`
- `INACTIVE`

### Example: Create a device

```bash
curl -X POST http://localhost:8080/api/v1/devices \
  -H "Content-Type: application/json" \
  -d '{"name": "iPhone 15 Pro", "brand": "Apple", "state": "AVAILABLE"}'
```

### Example: Patch state only

```bash
curl -X PATCH http://localhost:8080/api/v1/devices/{id} \
  -H "Content-Type: application/json" \
  -d '{"state": "IN_USE"}'
```
### Example: Filter by brand and state with pagination
```bash
curl "http://localhost:8080/api/v1/devices?brand=Apple&state=AVAILABLE&page=0&size=10&sort=createdAt,desc"
```
### Health Check (Actuator)
```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/info
http://localhost:8080/actuator/metrics
```
## Domain Rules

- `createdAt` is set automatically at creation and cannot be changed.
- `name` and `brand` cannot be updated while the device is `IN_USE`.
- A device with state `IN_USE` cannot be deleted.

## Running Tests

```bash
# Unit tests only (no Docker needed)
mvn test -Dtest="DeviceServiceTest"

# All tests including integration (requires Docker for Testcontainers)
mvn verify
```

Integration tests spin up a real PostgreSQL container via Testcontainers automatically.

## Design Decisions

- **UUID primary keys** — avoids sequential enumeration of resources.
- **Flyway migrations** — database schema is version-controlled and repeatable.
- **MapStruct** — compile-time type-safe mapping (zero reflection overhead).
- **`@Transactional(readOnly = true)`** on the service class with `@Transactional` on write methods — correct read/write split for JPA.
- **Global exception handler** — consistent error response format across all endpoints.
- **Multi-stage Dockerfile** — minimal runtime image using JRE Alpine, runs as non-root user.
- **Pagination with combined filtering** — `GET /devices` supports optional brand/state filters independently or combined, with page/size/sort parameters.
- **Spring Actuator** — exposes health, info and metrics endpoints for production observability.
- `saveAndFlush` in create — ensures `@CreationTimestamp` is populated before returning the response.

## Possible Future Improvements

- **Bulk device creation** — `POST /api/v1/devices/bulk` endpoint to create multiple devices in a single request.
- **Authentication & Authorization** — Spring Security with JWT or OAuth2; role-based access control.
- **Soft deletes** — flag records as deleted instead of permanently removing, useful for audit trails.
- **Audit logging** — track who changed what and when (Spring Data Envers).
- **Device state history** — keep a log of all state changes over time.
- **Optimistic locking** — `@Version` field to handle concurrent updates safely.
- **Caching** — Redis cache for frequently accessed devices.
- **Event publishing** — emit domain events on state changes via Kafka/RabbitMQ.
- **Metrics & observability** — Micrometer + Prometheus, OpenTelemetry distributed tracing.