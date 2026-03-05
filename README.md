# EventHub V2 - Evolution to Microservices

This project represents the evolution of the original EventHub technical challenge. While the first version focused on core
business rules in a single structure, V2 introduces concepts of distributed architecture, separation of
concerns, and asynchronous messaging.

## What changed in Version 2?

Unlike the first stage (detailed in [eventhub-api/TECHNICAL_CASE.md](eventhub-api/TECHNICAL_CASE.md)), this version
divides the system into two independent services:

1. **EventHub API**: Responsible for managing users, events, and creating ticket requests. Acts as the message
   producer.
2. **EventHub Payments**: A microservice dedicated exclusively to payment processing. It consumes requests
   from the queue, processes the transaction, and returns the final status.

## Technologies Used

- **Java 21** and **Spring Boot 3**
- **RabbitMQ**: Message broker.
- **PostgreSQL**: Main database.
- **MongoDB**: Transaction database.
- **Flyway**: Database migrations.
- **Spring Security + JWT**: Authentication and authorization.
- **Docker & Docker Compose**: Container orchestration.

## Repository Structure

- `/eventhub-api`: The core of the system, containing event business rules and security.
- `/eventHub-payments`: The payment worker that processes RabbitMQ queues.

## How to Run the Project

To run the entire ecosystem, you only need Docker installed.

1. **Variable Configuration**:
   Create a `.env` file in the project root with the following keys (matching the secrets defined in the deploy
   workflow):
   ```env
   API_PORT=15000
   PAYMENTS_PORT=15001
   DB_USERNAME=your_user
   DB_PASSWORD=your_password
   JWT_SECRET=your_jwt_secret
   RABBITMQ_USER=guest
   RABBITMQ_PASSWORD=guest
   MONGODB_URI=mongodb://mongodb:27017/payments
   MQ_EXCHANGE_NAME=eventhub.exchange
   MQ_QUEUE_INPUT=eventhub.payment.created
   MQ_QUEUE_OUTPUT=eventhub.payment.processed
   MQ_ROUTING_KEY_INPUT=payment.created
   MQ_ROUTING_KEY_OUTPUT=payment.processed
   ```

2. **Start Containers**:
   In the project root, run the command:
   ```bash
   docker-compose up -d --build
   ```

3. **Access Documentation**:
    - Swagger API: `http://localhost:15000/swagger-ui/index.html`

## Payment Flow

1. The user requests a ticket purchase in `eventhub-api`.
2. The API creates a ticket record with `PENDING` status and sends a message to the `payment.created` queue.
3. `eventHub-payments` receives the message, simulates processing, and saves the result in MongoDB.
4. The payment service sends a message back to the `payment.processed` queue.
5. The API consumes this response and updates the final ticket status (success or failure).
