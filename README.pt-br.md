# EventHub v2

O **EventHub** é um projeto para gerenciamento de eventos e venda de ingressos. O projeto nasceu como a resolução de
um [desafio técnico](eventhub-api/TECHNICAL_CASE.md).

A aplicação que originalmente era um monólito foi separada em microsserviços. A responsabilidade de transações
financeiras foi extraída para um *worker* de pagamentos independente, introduzindo comunicação assíncrona entre os
serviços utilizando **RabbitMQ** (nota: o processamento do pagamento na operadora é *mockado* para fins de
demonstração).

🚀 **Live Demo:** O projeto possui deploy automatizado (CI/CD) em uma VPS e online. Documentação dos endpoints:
https://eventhub.lbaba.com.br/swagger-ui/index.html

## Principais Funcionalidades

- **Autenticação e Autorização:** Registro e Login de usuários com segurança baseada em tokens JWT.
- **Gestão de Eventos:** CRUD completo para criação, listagem e gerenciamento de eventos.
- **Reserva de Ingressos:** Sistema de compra de ingressos vinculados aos usuários e eventos.
- **Processamento Assíncrono:** Fila de processamento de pagamentos utilizando RabbitMQ.
- **Idempotência:** Proteção contra duplicidade de requisições.

## Tecnologias

- Java 21
- Spring Boot 3.4 (Web, Data JPA, Data MongoDB, Security, AMQP, Actuator)
- PostgreSQL + Flyway (eventos e ingressos), MongoDB (transações de pagamento)
- RabbitMQ (mensageria assíncrona com DLQ + retry)
- Bucket4j (rate limiting)
- Micrometer + Prometheus + Grafana (observabilidade)
- Caddy (reverse proxy / TLS automático)
- Docker & Docker Compose
- GitHub Actions (CI/CD)

## Estrutura do Projeto

O projeto adota uma arquitetura baseada em microsserviços/módulos, dividida entre a API principal e o
serviço de processamento de pagamentos, comunicando-se de forma assíncrona.

```
EventHub-v2/
├── .github/workflows/         # CI/CD (Configurações do GitHub Actions)
├── docker-compose.yml         # Orquestração dos containers (base / produção)
├── docker-compose.override.yml # Overrides de dev (carregado automaticamente localmente)
├── monitoring/                # Config de scrape do Prometheus + provisioning do Grafana
├── pom.xml                    # Configuração raiz do projeto Maven
│
├── eventhub-contracts/        # Módulo compartilhado: DTOs + enums do contrato entre serviços
│
├── eventhub-api/              # Serviço Principal (REST API)
│   ├── Dockerfile
│   └── src/
│       ├── main/java/br/com/baba/eventHub/
│       │   ├── api/           # Entrypoints REST: Controllers, Handlers, Filters, Config (CORS)
│       │   └── core/          # Domínio: Models, Repositories, Services, Security (JWT) e DTOs
│       └── main/resources/
│           └── db/migration/  # Scripts de migração de banco de dados (Flyway)
│
└── eventHub-payments/         # Serviço worker para processamento de pagamentos
    ├── Dockerfile
    └── src/
        └── main/java/br/com/baba/eventHub/payments/
            ├── config/        # Configurações de mensageria (RabbitMQ)
            ├── core/          # Domínio específico de pagamentos: Models, DTOs e Repositories
            └── service/       # Regras de negócio e processamento de filas
```

## Como Executar

Para rodar a aplicação localmente, basta clonar o repositório e configurar as variáveis de ambiente necessárias.

### Pré-requisitos

- Docker instalado
- (Opcional) Java 21 e Maven instalados, caso queira rodar a aplicação fora dos containers.

### Clonar o Repositório

```
git clone https://github.com/LeonardoBaba/eventhub-v2.git
cd eventhub-v2
```

### Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto e preencha com as variáveis abaixo:

| **Variável**             | **Exemplo**                                                            | **Notas**                                          |
|--------------------------|------------------------------------------------------------------------|----------------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | `dev`                                                                  | `dev` ou `prod`                                    |
| `API_SERVER_PORT`        | `15500`                                                                |                                                    |
| `PAYMENTS_SERVER_PORT`   | `15550`                                                                |                                                    |
| `DB_URL`                 | `jdbc:postgresql://db:5432/eventhubdb`                                 | sobrescrita pela compose                           |
| `DB_USERNAME`            | `postgres`                                                             |                                                    |
| `DB_PASSWORD`            | `postgres`                                                             |                                                    |
| `JWT_SECRET`             | `seu_segredo_jwt_super_seguro`                                         |                                                    |
| `RABBITMQ_HOST`          | `rabbitmq`                                                             | sobrescrita pela compose                           |
| `RABBITMQ_PORT`          | `5672`                                                                 |                                                    |
| `RABBITMQ_USER`          | `guest`                                                                |                                                    |
| `RABBITMQ_PASSWORD`      | `guest`                                                                |                                                    |
| `MONGO_ROOT_USER`        | `root`                                                                 |                                                    |
| `MONGO_ROOT_PASSWORD`    | `root`                                                                 |                                                    |
| `MONGODB_URI`            | `mongodb://root:root@mongodb:27017/eventhub-payments?authSource=admin` | sobrescrita pela compose                           |
| `ADMIN_CREDENTIALS`      | `Admin:troque_isto`                                                    | **obrigatória** — `nome:senha`, bootstrap do admin |
| `ADMIN_EMAIL`            | `admin@example.com`                                                    | **obrigatória**                                    |
| `ADMIN_CPF`              | `11144477735`                                                          | **obrigatória** — CPF válido                       |
| `MONITORING_USER`        | `monitoring`                                                           | usuário do basic auth do `/actuator`               |
| `MONITORING_PASSWORD`    | `troque_isto`                                                          | **obrigatória**                                    |
| `GRAFANA_USER`           | `admin`                                                                | login do Grafana                                   |
| `GRAFANA_PASSWORD`       | `admin`                                                                | login do Grafana                                   |
| `FRONTEND_URL`           | `http://localhost:3000`                                               | origem permitida no CORS                           |
| `MQ_DLX_NAME`            | `eventhub.dlx`                                                         | opcional (tem default)                             |
| `RATELIMIT_RPM`          | `10`                                                                   | opcional (tem default)                             |
| `MQ_EXCHANGE_NAME`       | `eventhub.exchange`                                                    | opcional (tem default)                             |
| `MQ_QUEUE_INPUT`         | `eventhub.payment.created`                                             | opcional (tem default)                             |
| `MQ_QUEUE_OUTPUT`        | `eventhub.payment.processed`                                           | opcional (tem default)                             |
| `MQ_ROUTING_KEY_INPUT`   | `payment.created`                                                      | opcional (tem default)                             |
| `MQ_ROUTING_KEY_OUTPUT`  | `payment.processed`                                                    | opcional (tem default)                             |

*(As variáveis marcadas como **obrigatória** fazem o app falhar no startup (fail-fast) se ausentes. As "opcionais"
já têm default no código; valores de mensageria/host são sobrescritos pela Docker Compose em runtime.)*

### Executar

Com o arquivo `.env` configurado, inicie os containers utilizando o Docker Compose:

```
docker compose up -d --build
```

Isso fará o build das imagens da API e do serviço de Pagamentos, e subirá todos os serviços adjacentes (PostgreSQL,
MongoDB, RabbitMQ, Prometheus e Grafana).

Você poderá acessar a documentação da API via Swagger localmente em: `http://localhost:15500/swagger-ui/index.html`

## Observabilidade

A stack já vem com métricas prontas:

- **Spring Boot Actuator** expõe `/actuator/health` (usado pelos healthchecks dos containers) e
  `/actuator/prometheus`. Em produção este último é protegido por HTTP Basic auth (`MONITORING_USER` /
  `MONITORING_PASSWORD`).
- **Prometheus** raspa os dois serviços e guarda as séries temporais. Localmente fica em
  `http://localhost:9090` (não é publicado em produção — só acessível dentro da rede Docker).
- **Grafana** visualiza tudo (datasource + dashboard "EventHub Overview" auto-provisionados):
  - Local: `http://localhost:3000` (login `GRAFANA_USER` / `GRAFANA_PASSWORD`).
  - Produção: atrás do Caddy em `https://grafana.lbaba.com.br`.

Os profiles `dev` e `prod` (`SPRING_PROFILES_ACTIVE`) controlam quanto o Actuator expõe —
relaxado em dev, endurecido em prod.

## Deploy

O processo de deploy é totalmente automatizado via CI/CD. Qualquer *push* realizado na branch `master` aciona a pipeline
do `.github/workflows/deploy.yml`, que executa o seguinte fluxo:

1. Build da aplicação.
2. Geração e envio das imagens Docker (API e Payments) para o Docker Hub.
3. Deploy na VPS via conexão SSH.