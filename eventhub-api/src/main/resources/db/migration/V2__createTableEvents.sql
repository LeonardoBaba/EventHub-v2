CREATE TABLE events
(
    id           UUID PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    description  VARCHAR(255),
    date         TIMESTAMP    NOT NULL,
    location     VARCHAR(255) NOT NULL,
    capacity     INT          NOT NULL,
    status       VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    organizer_id UUID         NOT NULL,

    CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES users (id)
);