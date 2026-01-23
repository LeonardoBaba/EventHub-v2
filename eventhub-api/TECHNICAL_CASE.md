# 🎟️ Case Técnico: EventHub API

> Nota: Este é um case técnico fictício desenvolvido como exercício prático de programação e arquitetura de software. O
> cenário, os personagens e os requisitos de negócio foram gerados pelo Gemini (IA do Google) para simular um desafio
> real
> de processo seletivo para desenvolvedores Backend.
>

## 1. Visão Geral

Você foi contratado para desenvolver a API RESTful do **EventHub**, uma plataforma de gestão de eventos e venda de
ingressos. O objetivo é permitir que organizadores criem eventos e participantes garantam suas vagas, lidando com regras
de negócio de concorrência e notificações.

## 2. Stack Tecnológica Sugerida

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3+
- **Gerenciador de Dependências:** Maven
- **ORM:** JPA / Hibernate
- **Banco de Dados:** H2
- **Versionamento de Banco:** Flyway

## 3. Orientações Gerais

1. **Idioma:** O código deve ser escrito em **Inglês** (nomes de variáveis, classes, métodos, comentários).
2. **Padrões:** Utilize boas práticas de API REST (status codes corretos, verbos HTTP adequados, tratamento de
   exceções).
3. **Escopo:** Não há interface gráfica (Frontend); o foco é exclusivamente o Backend.

## 🚀 PRIMEIRA ETAPA: O Core da Aplicação

### 1. Usuários

Precisamos gerenciar os acessos à plataforma. Teremos três perfis de acesso: `PARTICIPANT`, `ORGANIZER` e `ADMIN`.

**1.1 Cadastro de Usuário (Público)**

- **Endpoint:** `POST /users`
- **Atributos:**
    - `name`: Texto (Obrigatório)
    - `email`: Texto (Obrigatório, deve ser um formato válido)
    - `cpf`: Texto (Obrigatório, formato válido, apenas números)
    - `password`: Texto (Obrigatório)
    - `role`: **Não deve ser enviado.** (Veja Regra de Negócio)
- **Regras de Negócio:**
    - O e-mail e o CPF devem ser únicos no sistema.
    - **Regra de Segurança:** Todo usuário criado através deste endpoint público deve ser atribuído automaticamente com
      o perfil `PARTICIPANT`.
    - Para criar usuários `ADMIN` ou `ORGANIZER` para testes, utilize uma migration do Flyway (SQL) para inserir os
      dados iniciais no banco.

**1.2 Consulta de Usuário (Restrito a Admin)**

- **Endpoint:** `GET /users/{cpf}`
- **Comportamento:** Retorna os dados públicos do usuário (Nome, E-mail e Role). Dados sensíveis como senha não devem
  trafegar.

### 2. Eventos

O coração da plataforma são os eventos criados pelos organizadores.

**2.1 Criação de Evento (Restrito a Admin e Organizer)**

- **Endpoint:** `POST /events`
- **Atributos:**
    - `title`: Texto (Obrigatório)
    - `description`: Texto (Opcional)
    - `date`: Data e Hora (Obrigatório)
    - `location`: Texto (Obrigatório)
    - `capacity`: Inteiro (Obrigatório, deve ser maior que 0)
    - `status`: Enum (`ACTIVE`, `CANCELLED`, `FINISHED`)
- **Regras de Negócio:**
    - A data do evento deve ser futura.
    - Apenas usuários com perfil `ORGANIZER` ou `ADMIN` podem criar eventos.

**2.2 Listagem de Eventos (Público)**

- **Endpoint:** `GET /events`
- **Comportamento:** Deve listar todos os eventos com status `ACTIVE`, com suporte a paginação.
- **Filtros:** Deve permitir filtrar por intervalo de datas (ex: `startDate` e `endDate`).

**2.3 Cancelamento de Evento (Restrito a Admin e Organizer)**

- **Endpoint:** `DELETE /events/{id}` (ou `PATCH` para atualização de status)
- **Comportamento:** Muda o status do evento para `CANCELLED`.
- **Regras de Negócio:** Eventos que já ocorreram (`FINISHED`) não podem ser cancelados.

### 3. Inscrição (Tickets)

Os usuários (Participantes) adquirem ingressos para os eventos.

**3.1 Realizar Inscrição (Autenticado)**

- **Endpoint:** `POST /events/{eventId}/tickets`
- **Atributos:**
    - `userId`: Identificador do usuário (pode vir do token ou do corpo, dependendo da implementação de segurança).
    - `ticketDate`: Data da compra (gerada pelo sistema).
- **Regras de Negócio:**
    - Um usuário não pode se inscrever duas vezes no mesmo evento.
    - Não é possível se inscrever em eventos com status `CANCELLED` ou `FINISHED`.
    - **Controle de Capacidade:** Não é possível se inscrever se o evento estiver lotado.
    - Ao confirmar a inscrição, o sistema deve disparar um e-mail de confirmação (apenas log/simulação).

## 💬 SEGUNDA ETAPA: Regras de Negócio Avançadas

O time de produto identificou uma necessidade crítica baseada no feedback dos organizadores. Leia o diálogo abaixo entre
o **Product Owner (Carlos)** e a **Tech Lead (Sofia)** e implemente a solução.

> Carlos: Sofia, os organizadores estão reclamando que perdem o controle de quando um evento lota. Eles queriam saber
> imediatamente quando os ingressos acabam para poderem abrir uma sessão extra ou comemorar.
>
>
> **Sofia:** Entendi. Podemos automatizar isso. Quando a última vaga for preenchida, o sistema dispara um alerta.
>
> **Carlos:** Exato! Um e-mail para o organizador avisando "Evento Esgotado". Ah, e outra coisa: eles querem saber se o
> evento está sendo um fracasso de vendas.
>
> **Sofia:** Como definimos "fracasso" tecnicamente?
>
> **Carlos:** Se faltarem **48 horas** para o evento começar e tivermos vendido **menos de 20%** dos ingressos. Nesse
> caso, também mandamos um alerta sugerindo uma promoção.
>
> **Sofia:** Perfeito. Vou pedir para implementarem esses dois gatilhos de notificação automática.
>

**Sua tarefa:**
Com base no diálogo, implemente:

1. **Alerta de Sold Out:** Enviar e-mail ao organizador do evento assim que a capacidade máxima for atingida durante uma
   compra.
2. **Alerta de Baixa Adesão:** Criar um mecanismo (Job agendado ou verificação recorrente) que verifica se o evento está
   a 48h de começar e tem menos de 20% de ocupação.

**Classe Utilitária para Simulação de Email:**
Use esta classe para não precisar configurar um servidor SMTP real.

```java
public class EmailNotificationService {
    public void send(String recipient, String subject, String message) {
        System.out.println("--------------------------------------------------");
        System.out.println("📧 SIMULATING EMAIL SENDING");
        System.out.println("To: " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        System.out.println("--------------------------------------------------");
    }
}

```

## 🏆 Bônus (Diferenciais)

Se quiser levar seu projeto para o próximo nível:

1. **Segurança Real:** Implemente **Spring Security** com autenticação via Token JWT. Garanta que apenas usuários
   logados comprem ingressos e apenas Admins/Organizers criem eventos.
2. **Containerização:** Crie um arquivo `docker-compose.yml` para rodar a aplicação e um banco de dados **PostgreSQL** (
   substituindo o H2).
3. **Documentação Viva:** Adicione o **Swagger/OpenAPI** (`springdoc-openapi`) para documentar seus endpoints e permitir
   testes via interface web.