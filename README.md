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

## Technologies

- Java 21
- Spring Boot 3.4 (Data JPA, Security, AMQP)
- PostgreSQL
- Flyway
- RabbitMQ
- Docker & Docker Compose
- GitHub Actions

## Project Structure

The project adopts an architecture based on microservices/modules, divided between the main API and the
payment processing service, communicating asynchronously.

```
EventHub-v2/
├── .github/workflows/     # CI/CD (GitHub Actions configuration)
├── docker-compose.yml     # Container orchestration (APIs, Messaging, Databases)
├── pom.xml                # Root Maven project configuration
│
├── eventhub-api/          # Main service (REST API)
│   ├── Dockerfile
│   └── src/
│       ├── main/java/br/com/baba/eventHub/
│       │   ├── api/       # REST entrypoints: Controllers, Handlers and Configurations (CORS)
│       │   └── core/      # Domain: Models, Repositories, Services, Security (JWT) and DTOs
│       └── main/resources/
│           └── db/migration/ # Database migration scripts (Flyway)
│
└── eventHub-payments/     # Worker service for payment processing
    ├── Dockerfile
    └── src/
        └── main/java/br/com/baba/eventHub/payments/
            ├── config/    # Messaging configuration (RabbitMQ)
            ├── core/      # Payment-specific domain: Models, DTOs and Repositories
            └── service/   # Business rules and queue processing
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

| **Variable**            | **Example**                                                            |
|-------------------------|------------------------------------------------------------------------|
| `API_PORT`              | `15000`                                                                |
| `PAYMENTS_PORT`         | `15001`                                                                |
| `DB_USERNAME`           | `postgres`                                                             |
| `DB_PASSWORD`           | `postgres`                                                             |
| `JWT_SECRET`            | `your_super_secure_jwt_secret`                                         |
| `RABBITMQ_USER`         | `guest`                                                                |
| `RABBITMQ_PASSWORD`     | `guest`                                                                |
| `MONGO_ROOT_USER`       | `root`                                                                 |
| `MONGO_ROOT_PASSWORD`   | `root`                                                                 |
| `MONGODB_URI`           | `mongodb://root:root@mongodb:27017/eventhub-payments?authSource=admin` |
| `MQ_EXCHANGE_NAME`      | `eventhub.exchange`                                                    |
| `MQ_QUEUE_INPUT`        | `eventhub.payment.created`                                             |
| `MQ_QUEUE_OUTPUT`       | `eventhub.payment.processed`                                           |
| `MQ_ROUTING_KEY_INPUT`  | `payment.created`                                                      |
| `MQ_ROUTING_KEY_OUTPUT` | `payment.processed`                                                    |

*(Note: The MQ variables (queues and routing) already have default values in the code, but can be overridden
in the `.env`)*

### Run

With the `.env` file configured, start the containers using Docker Compose:

```
docker compose up -d --build
```

This will build the API and Payments service images and bring up all adjacent services (PostgreSQL,
MongoDB and RabbitMQ).

You will be able to access the API documentation via Swagger locally at: `http://localhost:15000/swagger-ui/index.html`

## Deploy

The deploy process is fully automated via CI/CD. Any *push* made to the `master` branch triggers the pipeline
in `.github/workflows/deploy.yml`, which executes the following flow:

1. Build the application.
2. Generate and push Docker images (API and Payments) to Docker Hub.
3. Deploy to the VPS via SSH connection.
