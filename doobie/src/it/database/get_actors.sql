CREATE OR REPLACE FUNCTION runs.get_actors(
    i_first_name             TEXT,
    i_last_name              TEXT
) RETURNS TABLE (
                    actor_id                INTEGER,
                    first_name              VARCHAR(150),
                    last_name               VARCHAR(150)
                ) AS
$$
BEGIN
RETURN QUERY SELECT A.actor_id, A.first_name, A.last_name
                 FROM runs.actors A
                 WHERE
                     (i_first_name IS NULL OR A.first_name = i_first_name)
                   AND
                     (i_last_name IS NULL OR A.last_name = i_last_name)
                 ORDER BY A.actor_id ASC;
END;
$$
LANGUAGE plpgsql;