CREATE TABLE tickets
(
    id          UUID PRIMARY KEY,
    ticket_date TIMESTAMP NOT NULL,
    user_id     UUID      NOT NULL,
    event_id    UUID      NOT NULL,

    CONSTRAINT fk_events_buyer FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_events_id FOREIGN KEY (event_id) REFERENCES events (id)
);