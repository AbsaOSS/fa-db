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