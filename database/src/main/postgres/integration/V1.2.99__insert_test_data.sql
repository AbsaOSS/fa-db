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

INSERT INTO integration.actors VALUES
    (49, 'Pavel', 'Marek'),
    (50, 'Liza', 'Simpson');


INSERT INTO integration.other_types VALUES (
    1,
    'Top.Science.Astronomy',
    '192.168.1.1',
    '08:00:2b:01:02:03',
    'key=>value',
    '192.168.1/24',
    '{"key": "value"}',
    '{"key": "value"}',
    'b574cb0f-4790-4798-9b3f-824c7fab69dc',
    ARRAY[1,2,3]
);

INSERT INTO integration.date_time_types (
  offset_date_time, instant, zoned_date_time, local_date_time, local_date, local_time,
  sql_date, sql_time, sql_timestamp, util_date
) VALUES (
  '2004-10-19 10:23:54+02',
  '2004-10-19 10:23:54+02',
  '2004-10-19 10:23:54+02',
  '2004-10-19 10:23:54',
  '2004-10-19',
  '10:23:54',
  '2004-10-19',
  '10:23:54',
  '2004-10-19 10:23:54',
  '2004-10-19 10:23:54'
);
