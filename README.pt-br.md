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
- Spring Boot 3.4 (Data JPA, Security, AMQP)
- PostgreSQL
- Flyway
- RabbitMQ
- Docker & Docker Compose
- GitHub Actions

## Estrutura do Projeto

O projeto adota uma arquitetura baseada em microsserviços/módulos, dividida entre a API principal e o
serviço de processamento de pagamentos, comunicando-se de forma assíncrona.

```
EventHub-v2/
├── .github/workflows/     # CI/CD (Configurações do GitHub Actions)
├── docker-compose.yml     # Orquestração dos containers (APIs, Mensageria, Bancos de Dados)
├── pom.xml                # Configuração raiz do projeto Maven
│
├── eventhub-api/          # Serviço Principal (REST API)
│   ├── Dockerfile
│   └── src/
│       ├── main/java/br/com/baba/eventHub/
│       │   ├── api/       # Entrypoints REST: Controllers, Handlers e Configurações (CORS)
│       │   └── core/      # Domínio: Models, Repositories, Services, Security (JWT) e DTOs
│       └── main/resources/
│           └── db/migration/ # Scripts de migração de banco de dados (Flyway)
│
└── eventHub-payments/     # Serviço worker para processamento de pagamentos
    ├── Dockerfile
    └── src/
        └── main/java/br/com/baba/eventHub/payments/
            ├── config/    # Configurações de mensageria (RabbitMQ)
            ├── core/      # Domínio específico de pagamentos: Models, DTOs e Repositories
            └── service/   # Regras de negócio e processamento de filas
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

| **Variável**            | **Exemplo**                                                            |
|-------------------------|------------------------------------------------------------------------|
| `API_PORT`              | `15000`                                                                |
| `PAYMENTS_PORT`         | `15001`                                                                |
| `DB_USERNAME`           | `postgres`                                                             |
| `DB_PASSWORD`           | `postgres`                                                             |
| `JWT_SECRET`            | `seu_segredo_jwt_super_seguro`                                         |
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

*(Nota: As variáveis de MQ (filas e roteamento) já possuem valores padrão no código, mas podem ser sobrescritas
no `.env`)*

### Executar

Com o arquivo `.env` configurado, inicie os containers utilizando o Docker Compose:

```
docker compose up -d --build
```

Isso fará o build das imagens da API e do serviço de Pagamentos, e subirá todos os serviços adjacentes (PostgreSQL,
MongoDB e RabbitMQ).

Você poderá acessar a documentação da API via Swagger localmente em: `http://localhost:15000/swagger-ui/index.html`

## Deploy

O processo de deploy é totalmente automatizado via CI/CD. Qualquer *push* realizado na branch `master` aciona a pipeline
do `.github/workflows/deploy.yml`, que executa o seguinte fluxo:

1. Build da aplicação.
2. Geração e envio das imagens Docker (API e Payments) para o Docker Hub.
3. Deploy na VPS via conexão SSH.