CREATE OR REPLACE FUNCTION runs.error_if_not_one(p_input INT) 
RETURNS TABLE(
  status INT,
  status_text TEXT,
  input_value INT
) AS $$
BEGIN
  IF p_input != 1 THEN
    RETURN QUERY SELECT 99 AS status, 'error' AS status_text, NULL::INT AS input_value;
  ELSE
    RETURN QUERY SELECT 11 AS status, 'success' AS status_text, p_input AS input_value;
  END IF;
END;
$$ LANGUAGE plpgsql;