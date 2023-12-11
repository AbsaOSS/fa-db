CREATE TABLE runs.other_types (
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

INSERT INTO runs.other_types VALUES (
  1,
  'Top.Science.Astronomy',
  '192.168.1.1',
  '08:00:2b:01:02:03',
  'key=>value',
  '192.168.1/24',
  '{"key": "value"}',
  '{"key": "value"}',
  uuid_generate_v4(),
  ARRAY[1,2,3]
);

CREATE OR REPLACE FUNCTION runs.read_other_types(p_id INT)
RETURNS TABLE(
  id INT,
  ltree_col LTREE,
  inet_col INET,
  macaddr_col MACADDR,
  hstore_col HSTORE,
  cidr_col CIDR,
  json_col JSON,
  jsonb_col JSONB,
  uuid_col UUID,
  array_col INT[]
) AS $$
BEGIN
  RETURN QUERY SELECT * FROM runs.other_types T WHERE T.id = p_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION runs.insert_other_types(
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
    INSERT INTO runs.other_types VALUES (
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