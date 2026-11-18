CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE resources (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

CREATE TABLE reservations (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES users (id),
    resource_id BIGINT    NOT NULL REFERENCES resources (id),
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL,
    status      VARCHAR(20) NOT NULL
        CHECK (status IN ('EN_ATTENTE', 'VALIDEE', 'REFUSEE', 'ANNULEE')),
    CHECK (end_time > start_time)
);

CREATE INDEX idx_reservations_resource_time
    ON reservations (resource_id, start_time, end_time);

INSERT INTO resources (name, description) VALUES
    ('Serveur GPU 1', 'Serveur avec carte graphique pour l''entraînement de modèles'),
    ('Serveur GPU 2', 'Second serveur GPU'),
    ('VM de test', 'Machine virtuelle pour les essais');