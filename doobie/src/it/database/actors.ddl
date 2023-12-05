CREATE TABLE IF NOT EXISTS runs.actors
(
    actor_id
    INT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    first_name
    VARCHAR
(
    150
) NOT NULL,
    last_name VARCHAR
(
    150
) NOT NULL
    );