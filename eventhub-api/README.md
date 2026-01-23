> [🇧🇷 Leia em Português](README.pt-br.md)

# EventHub API 🎟️

### The project was developed as a solution for a [Technical Case](TECHNICAL_CASE.md).

Welcome to EventHub, a RESTful API for event management and ticket sales. This project allows organizers to create
events and participants to secure their spots, handling authentication, and automatic notifications.

## 🌐 Online Demo

The project is deployed on a VPS and can be tested publicly.

👉 **Access Swagger UI [here](http://72.62.104.105:15000/swagger-ui/index.html)**

## 🚀 Technologies Used

- Java 21
- Spring Boot 3
- JPA
- PostgreSQL
- Flyway
- Spring Security + JWT
- SpringDoc OpenAPI
- Docker
- JUnit
- Mockito

## ⚙️ Prerequisites

To run the project, you will need:

- **Docker**
- Or: **JDK 21** and **Maven** installed locally.
- Create a `.env` file in the project root

```code
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
SERVER_PORT=
```

## 🏃‍♂️ How to Run

### Option 1: Via Docker

1. Clone the repository.
2. In the project root, run:

```bash
docker-compose up --build
```

The API will be available at: [http://localhost:15000](http://localhost:15000)

### Option 2: Local Execution

1. Clone the repository.
2. Create a database in PostgreSQL named `eventhubdb`.
3. Add the connection URL to the `.env` file in the `DB_URL` variable.
4. In the project root, run:

```bash
./mvnw spring-boot:run
```

## 🛠️ Key Features

### 1. User Management

- **Registration:** User creation with unique CPF and Email validation.
- **Profiles:** `PARTICIPANT`, `ORGANIZER`, `ADMIN`.
- **Login:** Authentication via JWT.

### 2. Event Management

- **Creation:** Only Organizers and Admins can create events.
- **Listing:** Public listing of active events with pagination and date filters.
- **Cancellation:** Logical cancellation of events.

### 3. Ticket Sales

- **Purchase:** Authenticated users can buy tickets.
- **Validations:**
    - Capacity check.
    - User cannot buy twice for the same event.
    - Cancelled/Finished events blocked.
- **Notifications:** Simulation of email sending upon purchase confirmation.

### 4. Automated Notifications (Jobs)

- **Sold Out:** Triggers email to the organizer as soon as maximum capacity is reached.
- **Low Attendance:** Scheduled job that checks upcoming events (48h) with less than 20% occupancy and alerts the
  organizer.

## 🧪 Tests

Unit tests with JUnit and Mockito. To run the tests:

```bash
./mvnw test
```

## 📂 Project Structure

The project follows a layered architecture:

- `api/controller`: REST Endpoints.
- `api/handler`: Global exception handling.
- `core/service`: Business rules.
- `core/model`: JPA Entities.
- `core/repository`: Data access.
- `core/dto`: Data Transfer Objects (Records).
- `core/security`: JWT and Spring Security configurations.

---
Developed by [Leonardo Baba](https://www.linkedin.com/in/leonardo-baba-7b63821a0/)
