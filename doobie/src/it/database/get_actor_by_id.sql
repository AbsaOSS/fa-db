/*
 * Function: runs.get_actor_by_id
 * 
 * Description: 
 * This function retrieves an actor from the 'runs.actors' table by their ID.
 * 
 * Parameters:
 * i_actor_id - The ID of the actor to retrieve.
 * 
 * Returns: 
 * A table with the following columns:
 * actor_id - The ID of the actor.
 * first_name - The first name of the actor.
 * last_name - The last name of the actor.
 * 
 * Example:
 * SELECT * FROM runs.get_actor_by_id(1);
 * 
 * This will return the actor with ID 1, if he/she exists.
 */
CREATE OR REPLACE FUNCTION runs.get_actor_by_id(
    i_actor_id              INTEGER
) RETURNS TABLE (
    actor_id                INTEGER,
    first_name              VARCHAR(150),
    last_name               VARCHAR(150)
) AS
$$
BEGIN
    RETURN QUERY SELECT A.actor_id, A.first_name, A.last_name
    FROM runs.actors A
    WHERE A.actor_id = i_actor_id;
END;
$$
LANGUAGE plpgsql;