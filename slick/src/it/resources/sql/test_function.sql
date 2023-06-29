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


CREATE EXTENSION IF NOT EXISTS hstore;

CREATE EXTENSION IF NOT EXISTS ltree;

CREATE OR REPLACE FUNCTION public.test_function(
    IN  i_uuid1             UUID,
    IN  i_dateTime1         DATE,
    IN  i_dateTime2         TIME,
    IN  i_dateTime3         TIMESTAMP WITHOUT TIME ZONE,
    IN  i_dateTime4         INTERVAL,
    IN  i_dateTime5         TIMESTAMP WITH TIME ZONE,
    IN  i_dateTime6         TIMESTAMP WITH TIME ZONE,
    IN  i_range1            INT4RANGE,
    IN  i_ltree1            LTREE,
    IN  i_map1              HSTORE,
    IN  i_inet1             INET,
    IN  i_macaddr1          MACADDR,
    OUT uuid1               UUID,
    OUT dateTime1           DATE,
    OUT dateTime2           TIME,
    OUT dateTime3           TIMESTAMP WITHOUT TIME ZONE,
    OUT dateTime4           INTERVAL,
    OUT dateTime5           TIMESTAMP WITH TIME ZONE,
    OUT dateTime6           TIMESTAMP WITH TIME ZONE,
    OUT range1              INT4RANGE,
    OUT ltree1              LTREE,
    OUT map1                HSTORE,
    OUT inet1               INET,
    OUT macaddr1            MACADDR
) RETURNS record AS
$$
-------------------------------------------------------------------------------
--
-- Function: test_function(12)
--      A function to test Fa-Db Slick Posgres special time enhancement. Function wors as a mirror. Retruns what came in.
--
--
-- Returns:
--      input is returned unchanged
--
-------------------------------------------------------------------------------
DECLARE
BEGIN
    uuid1       := i_uuid1;
    dateTime1   := i_dateTime1;
    dateTime2   := i_dateTime2;
    dateTime3   := i_dateTime3;
    dateTime4   := i_dateTime4;
    dateTime5   := i_dateTime5;
    dateTime6   := i_dateTime6;
    range1      := i_range1;
    ltree1      := i_ltree1;
    map1        := i_map1;
    inet1       := i_inet1;
    macaddr1    := i_macaddr1;

    RETURN;
END;
$$
LANGUAGE plpgsql VOLATILE SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION test_function(
    UUID,
    DATE,
    TIME,
    TIMESTAMP WITHOUT TIME ZONE,
    INTERVAL,
    TIMESTAMP WITH TIME ZONE,
    TIMESTAMP WITH TIME ZONE,
    INT4RANGE,
    LTREE,
    HSTORE,
    INET,
    MACADDR
    ) TO postgres;
