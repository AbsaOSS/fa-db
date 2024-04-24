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

CREATE TABLE integration.other_types (
    id INT PRIMARY KEY,
    ltree_col LTREE,
    inet_col INET,
    macaddr_col MACADDR,
    hstore_col HSTORE,
    cidr_col CIDR,
    json_col JSON,
    jsonb_col JSONB,
    uuid_col UUID,
    array_col INT[]
);
