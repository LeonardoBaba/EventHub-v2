# EventHub v2

**EventHub** is a project for event management and ticket sales. The project was born as the resolution of
a [detailed technical challenge](eventhub-api/TECHNICAL_CASE.md).

The application, which was originally a monolith, was split into microservices. The responsibility for financial
transactions was extracted into an independent payments *worker*, introducing asynchronous communication between
the services using **RabbitMQ** (note: payment processing on the operator side is *mocked* for demonstration
purposes).

🚀 **Live Demo:** The project has automated deployment (CI/CD) on a VPS and is online. Endpoint documentation:
https://eventhub.lbaba.com.br/swagger-ui/index.html

## Main Features

- **Authentication and Authorization:** User registration and login with JWT token-based security.
- **Event Management:** Full CRUD for creating, listing, and managing events.
- **Ticket Booking:** Ticket purchase system linked to users and events.
- **Asynchronous Processing:** Payment processing queue using RabbitMQ.
- **Idempotency:** Protection against duplicate requests.

## Tech Stack

- Java 21
- Spring Boot 3.4 (Web, Data JPA, Data MongoDB, Security, AMQP, Actuator)
- PostgreSQL + Flyway (events & tickets), MongoDB (payment transactions)
- RabbitMQ (async messaging with DLQ + retry)
- Bucket4j (rate limiting)
- Micrometer + Prometheus + Grafana (observability)
- Caddy (reverse proxy / automatic TLS)
- Docker & Docker Compose
- GitHub Actions (CI/CD)

## Project Structure

The project adopts an architecture based on microservices/modules, divided between the main API and the
payment processing service, communicating asynchronously.

```
EventHub-v2/
├── .github/workflows/         # CI/CD (GitHub Actions configuration)
├── docker-compose.yml         # Container orchestration (base / production)
├── docker-compose.override.yml # Dev-only overrides (auto-loaded locally)
├── monitoring/                # Prometheus scrape config + Grafana provisioning
├── pom.xml                    # Root Maven project configuration
│
├── eventhub-contracts/        # Shared module: wire DTOs + enums between services
│
├── eventhub-api/              # Main service (REST API)
│   ├── Dockerfile
│   └── src/
│       ├── main/java/br/com/baba/eventHub/
│       │   ├── api/           # REST entrypoints: Controllers, Handlers, Filters, Config (CORS)
│       │   └── core/          # Domain: Models, Repositories, Services, Security (JWT) and DTOs
│       └── main/resources/
│           └── db/migration/  # Database migration scripts (Flyway)
│
└── eventHub-payments/         # Worker service for payment processing
    ├── Dockerfile
    └── src/
        └── main/java/br/com/baba/eventHub/payments/
            ├── config/        # Messaging configuration (RabbitMQ)
            ├── core/          # Payment-specific domain: Models, DTOs and Repositories
            └── service/       # Business rules and queue processing
```

## How to Run

To run the application locally, just clone the repository and configure the necessary environment variables.

### Prerequisites

- Docker installed
- (Optional) Java 21 and Maven installed, in case you want to run the application outside the containers.

### Clone the Repository

```
git clone https://github.com/LeonardoBaba/eventhub-v2.git
cd eventhub-v2
```

### Environment Variables

Create a `.env` file in the project root and fill it with the variables below:

| **Variable**             | **Example**                                                            | **Notes**                                            |
|--------------------------|------------------------------------------------------------------------|------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | `dev`                                                                  | `dev` or `prod`                                      |
| `API_SERVER_PORT`        | `15500`                                                                |                                                      |
| `PAYMENTS_SERVER_PORT`   | `15550`                                                                |                                                      |
| `DB_URL`                 | `jdbc:postgresql://db:5432/eventhubdb`                                 | overridden by compose                                |
| `DB_USERNAME`            | `postgres`                                                             |                                                      |
| `DB_PASSWORD`            | `postgres`                                                             |                                                      |
| `JWT_SECRET`             | `your_super_secure_jwt_secret`                                         |                                                      |
| `RABBITMQ_HOST`          | `rabbitmq`                                                             | overridden by compose                                |
| `RABBITMQ_PORT`          | `5672`                                                                 |                                                      |
| `RABBITMQ_USER`          | `guest`                                                                |                                                      |
| `RABBITMQ_PASSWORD`      | `guest`                                                                |                                                      |
| `MONGO_ROOT_USER`        | `root`                                                                 |                                                      |
| `MONGO_ROOT_PASSWORD`    | `root`                                                                 |                                                      |
| `MONGODB_URI`            | `mongodb://root:root@mongodb:27017/eventhub-payments?authSource=admin` | overridden by compose                                |
| `ADMIN_CREDENTIALS`      | `Admin:change_me`                                                      | **required** — `name:password`, bootstraps the admin |
| `ADMIN_EMAIL`            | `admin@example.com`                                                    | **required**                                         |
| `ADMIN_CPF`              | `11144477735`                                                          | **required** — valid CPF                             |
| `MONITORING_USER`        | `monitoring`                                                           | basic-auth user for `/actuator`                      |
| `MONITORING_PASSWORD`    | `change_me`                                                            | **required**                                         |
| `GRAFANA_USER`           | `admin`                                                                | Grafana login                                        |
| `GRAFANA_PASSWORD`       | `admin`                                                                | Grafana login                                        |
| `FRONTEND_URL`           | `http://localhost:3000`                                                | CORS allowed origin                                  |
| `MQ_DLX_NAME`            | `eventhub.dlx`                                                         | optional (has default)                               |
| `RATELIMIT_RPM`          | `10`                                                                   | optional (has default)                               |
| `MQ_EXCHANGE_NAME`       | `eventhub.exchange`                                                    | optional (has default)                               |
| `MQ_QUEUE_INPUT`         | `eventhub.payment.created`                                             | optional (has default)                               |
| `MQ_QUEUE_OUTPUT`        | `eventhub.payment.processed`                                           | optional (has default)                               |
| `MQ_ROUTING_KEY_INPUT`   | `payment.created`                                                      | optional (has default)                               |
| `MQ_ROUTING_KEY_OUTPUT`  | `payment.processed`                                                    | optional (has default)                               |

*(Variables marked **required** make the app fail-fast on startup if missing. The ones marked "optional" already
have defaults in the code; messaging/host values are overridden by Docker Compose at runtime.)*

### Run

With the `.env` file configured, start the containers using Docker Compose:

```
docker compose up -d --build
```

This will build the API and Payments service images and bring up all adjacent services (PostgreSQL,
MongoDB, RabbitMQ, Prometheus and Grafana).

You will be able to access the API documentation via Swagger locally at: `http://localhost:15500/swagger-ui/index.html`

## Observability

The stack ships with metrics out of the box:

- **Spring Boot Actuator** exposes `/actuator/health` (used by the container healthchecks) and
  `/actuator/prometheus`. In production the latter is protected by HTTP Basic auth (`MONITORING_USER` /
  `MONITORING_PASSWORD`).
- **Prometheus** scrapes both services and stores the time series. Locally it is available at
  `http://localhost:9090` (it is not published in production — only reachable inside the Docker network).
- **Grafana** visualizes everything (datasource + an "EventHub Overview" dashboard are auto-provisioned):
    - Local: `http://localhost:3000` (login `GRAFANA_USER` / `GRAFANA_PASSWORD`).
    - Production: behind Caddy at `https://grafana.lbaba.com.br`.

`dev` and `prod` Spring profiles (`SPRING_PROFILES_ACTIVE`) control how much Actuator exposes —
relaxed in dev, hardened in prod.

## Deploy

The deploy process is fully automated via CI/CD. Any *push* made to the `master` branch triggers the pipeline
in `.github/workflows/deploy.yml`, which executes the following flow:

1. Build the application.
2. Generate and push Docker images (API and Payments) to Docker Hub.
3. Deploy to the VPS via SSH connection.
