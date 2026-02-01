> [🇺🇸 Read in English](README.md)

# EventHub API 🎟️

### O projeto foi desenvolvido como solução para um [Case Técnico](TECHNICAL_CASE.md).

Bem-vindo ao EventHub, uma API RESTful para gestão de eventos e venda de ingressos. Este projeto permite que
organizadores criem eventos e participantes garantam suas vagas, lidando com autenticação e
notificações automáticas.

## 🌐 Demonstração Online

O projeto está com deploy realizado em uma VPS e pode ser testado publicamente.

👉 **Acesse o Swagger UI [aqui](http://72.62.104.105:15000/swagger-ui/index.html)**

## 🚀 Tecnologias Utilizadas

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

## ⚙️ Pré-requisitos

Para rodar o projeto, você precisará de:

- **Docker**
- Ou: **JDK 21** e **Maven** instalados localmente.
- Criar um arquivo `.env` na raiz do projeto

```code
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
API_SERVER_PORT=
PAYMENTS_SERVER_PORT=
```

## 🏃‍♂️ Como Executar

### Opção 1: Via Docker

1. Clone o repositório.
2. Na raiz do projeto, execute:

```bash
docker-compose up --build
```

A API estará disponível em: [http://localhost:15000](http://localhost:15000)

### Opção 2: Execução Local

1. Clone o repositório.
2. Crie um banco de dados no PostgreSQL com o nome `eventhubdb`.
3. Adicione a URL de conexão no arquivo `.env` na variável `DB_URL`.
4. Na raiz do projeto, execute:

```bash
./mvnw spring-boot:run
```

## 🛠️ Funcionalidades Principais

### 1. Gestão de Usuários

- **Cadastro:** Criação de usuários com validação de CPF e Email únicos.
- **Perfis:** `PARTICIPANT`, `ORGANIZER`, `ADMIN`.
- **Login:** Autenticação via JWT.

### 2. Gestão de Eventos

- **Criação:** Apenas Organizadores e Admins podem criar eventos.
- **Listagem:** Listagem pública de eventos ativos com paginação e filtros de data.
- **Cancelamento:** Cancelamento lógico de eventos.

### 3. Venda de Tickets

- **Compra:** Usuários autenticados podem comprar ingressos.
- **Validações:**
    - Verificação de capacidade.
    - Usuário não pode comprar duas vezes para o mesmo evento.
    - Eventos cancelados/finalizados bloqueados.
- **Notificações:** Simulação de envio de e-mail ao confirmar compra.

### 4. Notificações Automatizadas (Jobs)

- **Sold Out:** Dispara e-mail ao organizador assim que a lotação máxima é atingida.
- **Baixa Adesão:** Job agendado que verifica eventos próximos (48h) com menos de 20% de ocupação e alerta o
  organizador.

## 🧪 Testes

Testes unitários com JUnit e Mockito. Para executar os testes:

```bash
./mvnw test
```

## 📂 Estrutura do Projeto

O projeto segue uma arquitetura em camadas:

- `api/controller`: Endpoints REST.
- `api/handler`: Tratamento global de exceções.
- `core/service`: Regras de negócio.
- `core/model`: Entidades JPA.
- `core/repository`: Acesso a dados.
- `core/dto`: Objetos de transferência de dados (Records).
- `core/security`: Configurações de JWT e Spring Security.

---
Desenvolvido por [Leonardo Baba](https://www.linkedin.com/in/leonardo-baba-7b63821a0/)
