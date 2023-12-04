-- Function: runs.create_actor(TEXT, TEXT)
-- This function creates a new actor in the 'runs.actors' table.

-- Parameters:
-- i_first_name (IN): The first name of the actor.
-- i_last_name (IN): The last name of the actor.

-- Output:
-- status (OUT): The status of the operation. Returns 11 if the actor is successfully created.
-- status_text (OUT): The status text of the operation. Returns 'Actor created' if the actor is successfully created.

-- Returns:
-- A record containing the status and status text of the operation.

-- Example:
-- SELECT * FROM runs.create_actor('John', 'Doe');

CREATE OR REPLACE FUNCTION runs.create_actor(
    IN  i_first_name           TEXT,
    IN  i_last_name            TEXT,
    OUT status                 INTEGER,
    OUT status_text            TEXT,
    OUT o_actor_id             INTEGER
) RETURNS record AS
$$
BEGIN
    INSERT INTO runs.actors(first_name, last_name)
    VALUES (i_first_name, i_last_name)
    RETURNING actor_id INTO o_actor_id;

    status := 11;
    status_text := 'Actor created';

    RETURN;
END;
$$
LANGUAGE plpgsql;