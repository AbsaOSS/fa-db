/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE OR REPLACE FUNCTION integration.get_actors_by_lastname(
    IN  i_last_name          TEXT,
    IN  i_first_name         TEXT = NULL,

    OUT status               INTEGER,
    OUT status_text          TEXT,

    OUT actor_id             INT,
    OUT first_name           character varying(150),
    OUT last_name            character varying(150)
) RETURNS SETOF record AS
$$
DECLARE
BEGIN

    IF i_first_name IS NULL THEN
        status := 11;
        status_text := 'OK, match on last name only';

        RETURN QUERY
        SELECT status, status_text,
               A.actor_id, A.first_name, A.last_name
        FROM integration.actors AS A
        WHERE A.last_name = i_last_name;

    ELSE
        status := 12;
        status_text := 'OK, full match';

        RETURN QUERY
        SELECT status, status_text,
               A.actor_id, A.first_name, A.last_name
        FROM integration.actors AS A
        WHERE A.first_name = i_first_name
          AND A.last_name = i_last_name;

    END IF;

    IF NOT FOUND THEN
        status := 41;
        status_text := 'No actor found';
        RETURN NEXT;
        RETURN;
    END IF;

END;
$$
LANGUAGE plpgsql;
