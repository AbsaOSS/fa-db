CREATE TABLE runs.date_time_types (
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
);

INSERT INTO runs.date_time_types (
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


CREATE OR REPLACE FUNCTION runs.get_all_date_time_types(p_id INT)
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
                 FROM runs.date_time_types T limit p_id;
END;
$$ LANGUAGE plpgsql;