# Observability Service

This repository now contains a starter Java service for the GTBS Audit & Observability component.

The goal is not to be "finished". The goal is to give you a clean, understandable baseline that supports:

- immutable audit logging
- Kafka event ingestion
- PostgreSQL persistence
- health checks
- Prometheus metrics
- OpenTelemetry tracing hooks

## 1. What This Service Does

In your architecture, this service is responsible for:

- consuming booking lifecycle events
- consuming route or policy change events
- storing an append-only audit trail
- exposing read APIs for audit lookup
- exposing metrics and traces so the platform can be monitored

It is not on the booking correctness path.

That means:

- if this service is slow, bookings should still work
- if Kafka is temporarily unavailable, bookings should still not lose correctness
- this service is for compliance, debugging, incident review, and operations

## 2. Recommended Beginner Setup

Use these versions locally:

- Java 21 LTS or newer
- Maven 3.9+
- Docker Desktop
- IntelliJ IDEA Community or Ultimate

Your machine already has Java installed, but Maven is not installed in this repo environment.

If you are on macOS and use Homebrew:

```bash
brew install maven
```

Check your tools:

```bash
java -version
mvn -version
docker --version
```

## 3. Why Java + Spring Boot Here

For this project, Spring Boot gives you the things you need quickly:

- REST endpoints
- Kafka integration
- database access
- health endpoints
- metrics and tracing support
- simple configuration through `application.yml`

If you have not used Java in years, Spring Boot is the easiest way back in.

## 4. Project Structure

```text
observability-service/
├── docker-compose.yml
├── otel-collector-config.yaml
├── pom.xml
├── prometheus.yml
└── src
    ├── main
    │   ├── java/com/gtbs/observability
    │   │   ├── ObservabilityServiceApplication.java
    │   │   ├── audit
    │   │   │   ├── api
    │   │   │   ├── domain
    │   │   │   ├── repository
    │   │   │   └── service
    │   │   └── ingest
    │   └── resources
    │       ├── application.yml
    │       └── db/migration
    └── test
```

### What Each Package Means

- `audit/domain`: your database-backed core audit model
- `audit/repository`: Spring Data access to Postgres
- `audit/service`: business logic for storing and reading audit records
- `audit/api`: REST controllers and response DTOs
- `ingest`: Kafka consumers and event payload classes

This layout is intentionally simple. It is a good starting structure for a small service.

## 5. How To Run Locally

Start infrastructure first:

```bash
docker compose up -d
```

Then run the Spring Boot app:

```bash
mvn spring-boot:run
```

Useful local URLs:

- app health: `http://localhost:8080/actuator/health`
- Prometheus metrics: `http://localhost:8080/actuator/prometheus`
- audit lookup: `http://localhost:8080/api/v1/audit/bookings/{bookingId}`
- Kafka UI: `http://localhost:8081`
- Jaeger UI: `http://localhost:16686`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Grafana default login:

- username: `admin`
- password: `admin`

## 6. Local Infrastructure

`docker-compose.yml` starts:

- PostgreSQL for audit storage
- Kafka for event ingestion
- Kafka UI for debugging topics
- OpenTelemetry Collector for traces
- Jaeger for trace visualization
- Prometheus for metrics scraping
- Grafana for dashboards

This is more than enough for a student project and closely matches your architecture document.

## 7. Important Concepts To Keep In Mind

### Append-only audit logs

Audit records should not be edited after creation.

In practice:

- insert new audit rows
- never update old audit rows unless fixing bad test data
- keep original payloads when possible

### Service boundaries

This service should not own booking business logic.

It should consume and observe:

- booking events
- policy events
- route changes
- operational signals

### Observability is not just logging

You need three things:

- logs: what happened
- metrics: how often and how fast
- traces: how requests moved between services

That is why this starter includes Actuator, Prometheus, and OpenTelemetry support.

## 8. Suggested Next Steps

After you get this running, build in this order:

1. produce fake booking and policy events into Kafka
2. verify they are stored in `audit_records`
3. add search endpoints for booking ID, segment ID, and region
4. add signed or tamper-evident audit export if your coursework needs stronger compliance guarantees
5. add integration tests with Testcontainers

## 9. Notes About The Rest Of GTBS

For the whole platform, I would keep the Java service structure roughly like this:

- `booking-service`
- `journey-compatibility-service`
- `route-management-service`
- `traffic-authority-service`
- `audit-observability-service`

For each Java service, use the same internal package pattern:

- `api`
- `application`
- `domain`
- `repository`
- `config`
- `messaging`

Keeping the shape consistent across services matters more than finding the "perfect" folder layout.
