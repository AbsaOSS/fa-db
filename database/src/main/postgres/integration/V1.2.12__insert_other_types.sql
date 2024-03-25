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

CREATE OR REPLACE FUNCTION integration.insert_other_types(
  p_id INT,
  p_ltree_col LTREE,
  p_inet_col INET,
  p_macaddr_col MACADDR,
  p_hstore_col HSTORE,
  p_cidr_col CIDR,
  p_json_col JSON,
  p_jsonb_col JSONB,
  p_uuid_col UUID,
  p_array_col INT[]
) RETURNS TABLE(
  status INT,
  status_text TEXT,
  o_id INT
) AS $$
BEGIN
  BEGIN
    INSERT INTO integration.other_types VALUES (
      p_id,
      p_ltree_col,
      p_inet_col,
      p_macaddr_col,
      p_hstore_col,
      p_cidr_col,
      p_json_col,
      p_jsonb_col,
      p_uuid_col,
      p_array_col
    );
    status := 11;
    status_text := 'ok';
    o_id := p_id;
  EXCEPTION WHEN unique_violation THEN
    status := 31;
    status_text := 'data conflict';
  END;
  RETURN NEXT;
END;
$$ LANGUAGE plpgsql;
