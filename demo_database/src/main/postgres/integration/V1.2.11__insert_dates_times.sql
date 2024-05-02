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

CREATE OR REPLACE FUNCTION integration.insert_dates_times(
    IN p_offset_date_time TIMESTAMPTZ,
    IN p_instant TIMESTAMPTZ,
    IN p_zoned_date_time TIMESTAMPTZ,
    IN p_local_date_time TIMESTAMP,
    IN p_local_date DATE,
    IN p_local_time TIME,
    IN p_sql_date DATE,
    IN p_sql_time TIME,
    IN p_sql_timestamp TIMESTAMP,
    IN p_util_date DATE,
    OUT status INTEGER,
    OUT status_text TEXT,
    OUT o_id INTEGER
) RETURNS record AS $$
BEGIN
    INSERT INTO integration.date_time_types (
        offset_date_time,
        instant,
        zoned_date_time,
        local_date_time,
        local_date,
        local_time,
        sql_date,
        sql_time,
        sql_timestamp,
        util_date
    ) VALUES (
                 p_offset_date_time,
                 p_instant,
                 p_zoned_date_time,
                 p_local_date_time,
                 p_local_date,
                 p_local_time,
                 p_sql_date,
                 p_sql_time,
                 p_sql_timestamp,
                 p_util_date
             ) RETURNING id INTO o_id;

    status := 11;
    status_text := 'OK';

    RETURN;
END;
$$ LANGUAGE plpgsql;
