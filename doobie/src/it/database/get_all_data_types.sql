CREATE TABLE runs.all_data_types (
                                     col_smallint SMALLINT,
                                     col_integer INTEGER,
                                     col_bigint BIGINT,
                                     col_decimal DECIMAL(5, 2),
                                     col_numeric NUMERIC(10, 5),
                                     col_real REAL,
                                     col_double_precision DOUBLE PRECISION,
                                     col_money MONEY,
                                     col_char CHAR(10),
                                     col_varchar VARCHAR(50),
                                     col_text TEXT,
                                     col_bytea BYTEA,
                                     col_timestamp TIMESTAMP,
                                     col_date DATE,
                                     col_time TIME,
                                     col_boolean BOOLEAN,
                                     col_bit BIT(4),
                                     col_uuid UUID,
                                     col_json JSON,
                                     col_jsonb JSONB,
                                     col_int_array INTEGER[],
                                     col_text_array TEXT[]
);

INSERT INTO runs.all_data_types (
    col_smallint, col_integer, col_bigint, col_decimal, col_numeric, col_real, col_double_precision,
    col_money, col_char, col_varchar, col_text, col_timestamp, col_date, col_time, col_boolean,
    col_bit, col_uuid, col_json, col_jsonb, col_int_array, col_text_array
) VALUES (
             1, 2, 3, 4.5, 6.789, 7.8, 9.01,
             100.00, 'char', 'varchar', 'text', CURRENT_TIMESTAMP, CURRENT_DATE, CURRENT_TIME, true,
             B'1010', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '{"key": "value"}', '{"key": "value"}',
             ARRAY[1,2,3], ARRAY['text1', 'text2', 'text3']
         );

CREATE OR REPLACE FUNCTION runs.get_all_data_types(p_col_integer INTEGER)
RETURNS SETOF runs.all_data_types
AS
$$
BEGIN
    RETURN QUERY SELECT * FROM runs.all_data_types WHERE col_integer = p_col_integer;
END;
$$
LANGUAGE plpgsql;