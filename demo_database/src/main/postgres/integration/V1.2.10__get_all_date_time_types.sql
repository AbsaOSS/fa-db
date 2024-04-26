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

CREATE OR REPLACE FUNCTION integration.get_all_date_time_types(p_id INT)
    RETURNS TABLE(
                     offset_date_time TIMESTAMPTZ,
                     instant TIMESTAMPTZ,
                     zoned_date_time TIMESTAMPTZ,
                     local_date_time TIMESTAMP,
                     local_date DATE,
                     local_time TIME,
                     sql_date DATE,
                     sql_time TIME,
                     sql_timestamp TIMESTAMP,
                     util_date TIMESTAMP
                 ) AS $$
BEGIN
    RETURN QUERY SELECT
                     T.offset_date_time,
                     T.instant,
                     T.zoned_date_time,
                     T.local_date_time,
                     T.local_date,
                     T.local_time,
                     T.sql_date,
                     T.sql_time,
                     T.sql_timestamp,
                     T.util_date
                 FROM integration.date_time_types T limit p_id;
END;
$$ LANGUAGE plpgsql;
