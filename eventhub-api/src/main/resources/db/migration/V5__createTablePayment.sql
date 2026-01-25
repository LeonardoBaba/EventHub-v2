CREATE TABLE payments
(
    id            UUID PRIMARY KEY,
    ticket_id     UUID         NOT NULL,
    status        VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    card_token    VARCHAR(100) NOT NULL,
    installments  INTEGER      NOT NULL,
    creation_date TIMESTAMP    NOT NULL,

    CONSTRAINT fk_tickets_id FOREIGN KEY (ticket_id) REFERENCES tickets (id)
);