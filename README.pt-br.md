# EventHub V2 - Evolução para Microsserviços

Este projeto representa a evolução do desafio técnico original do EventHub. Enquanto a primeira versão focava nas regras
de negócio principais em uma estrutura única, a V2 introduz conceitos de arquitetura distribuída, separação de
responsabilidades e mensageria assíncrona.

## O que mudou na Versão 2?

Diferente da primeira etapa (detalhada em [eventhub-api/TECHNICAL_CASE.md](eventhub-api/TECHNICAL_CASE.md)), esta versão
divide o sistema em dois serviços independentes:

1. **EventHub API**: Responsável pela gestão de usuários, eventos e criação de pedidos de tickets. Atua como o produtor
   de mensagens.
2. **EventHub Payments**: Um microsserviço dedicado exclusivamente ao processamento de pagamentos. Ele consome pedidos
   da fila, processa a transação e devolve o status final.

## Tecnologias Utilizadas

- **Java 21** e **Spring Boot 3**
- **RabbitMQ**: Broker de mensagens.
- **PostgreSQL**: Banco de dados principal.
- **MongoDB**: Banco de dados de transações.
- **Flyway**: Migrações de banco de dados.
- **Spring Security + JWT**: Autenticação e autorização.
- **Docker & Docker Compose**: Orquestração de containers.

## Estrutura do Repositório

- `/eventhub-api`: O núcleo do sistema, contendo as regras de negócio de eventos e segurança.
- `/eventHub-payments`: O worker de pagamentos que processa as filas do RabbitMQ.

## Como Executar o Projeto

Para rodar todo o ecossistema, você precisará apenas do Docker instalado.

1. **Configuração de Variáveis**:
   Crie um arquivo `.env` na raiz do projeto com as seguintes chaves (conforme os segredos definidos no workflow de
   deploy):
   ```env
   API_PORT=15000
   PAYMENTS_PORT=15001
   DB_USERNAME=seu_usuario
   DB_PASSWORD=sua_senha
   JWT_SECRET=seu_segredo_jwt
   RABBITMQ_USER=guest
   RABBITMQ_PASSWORD=guest
   MONGODB_URI=mongodb://mongodb:27017/payments
   MQ_EXCHANGE_NAME=eventhub.exchange
   MQ_QUEUE_INPUT=eventhub.payment.created
   MQ_QUEUE_OUTPUT=eventhub.payment.processed
   MQ_ROUTING_KEY_INPUT=payment.created
   MQ_ROUTING_KEY_OUTPUT=payment.processed
   ```

2. **Subir os Containers**:
   Na raiz do projeto, execute o comando:
   ```bash
   docker-compose up -d --build
   ```

3. **Acessar a Documentação**:
    - API Swagger: `http://localhost:15000/swagger-ui/index.html`

## Fluxo de Pagamento

1. O usuário solicita a compra de um ticket na `eventhub-api`.
2. A API cria um registro de ticket com status `PENDING` e envia uma mensagem para a fila `payment.created`.
3. O `eventHub-payments` recebe a mensagem, simula o processamento e salva o resultado no MongoDB.
4. O serviço de pagamentos envia uma mensagem de volta para a fila `payment.processed`.
5. A API consome essa resposta e atualiza o status final do ticket (sucesso ou falha).

---