#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import dataclasses
import argparse
import logging
import os
import psycopg2
import copy


@dataclasses.dataclass
class PostgresDBConn:
    """This dataclass contains all information related to making a connection to Postgres DB."""
    username: str
    password: str
    host: str
    database: str
    port: int


def parse_args() -> argparse.Namespace:
    """CLI args parsing function."""
    parser = argparse.ArgumentParser(
        description="Deploys structures to DB. (script version: 1.0)",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument(
        "-ph", "--host",
        help="database server host (default: \"localhost\")",
        default="localhost",
    )
    parser.add_argument(
        "-p", "--port",
        help="database server port (default: \"5432\")",
        default="5432",
    )
    parser.add_argument(
        "-d", "--dbname",
        help="database name to connect to (default: \"ursa_unify_db\")",
        required=True,
    )
    parser.add_argument(
        "-U", "--username",
        help="database user name, should be a high privileged account (default: \"postgres\")",
        default="postgres",
    )
    parser.add_argument(
        "-W", "--password",
        help="database user password",
        required=True,
    )
    parser.add_argument(
        "-dir", "--dir",
        help="the directory of database source files (default: current directory)",
        default=os.getcwd()
    )
    parser.add_argument(
        "--create-db",
        action="store_true",
        help="creates the target database (runs the scripts in the source root starting with '00_', which is/are "
             "expected to create the db",
    )

    return parser.parse_args()


def execute_sql(conn_config: PostgresDBConn, sql: str) -> None:
    conn = psycopg2.connect(
        database=conn_config.database,
        user=conn_config.username,
        password=conn_config.password,
        host=conn_config.host,
        port=conn_config.port
    )

    conn.autocommit = True
    cursor = conn.cursor()

    cursor.execute(sql)

    conn.commit()
    conn.close()


def read_file(file_name: str) -> str:
    logging.debug(f"    - reading file `{file_name}`")
    file = open(file_name, "r")
    result = file.read()
    file.close()
    return result


def ensure_trailing_slash(path: str) -> str:
    if path.endswith('/'):
        return path
    else:
        return path + '/'


def process_dir(directory: str, conn_config: PostgresDBConn, create_db: bool) -> None:
    logging.info(f"Picking up source files from directory `{directory}`")
    public_schema = "public"
    root = next(os.walk(directory), (None, [], []))
    schemas = list(root[1])
    files = list(filter(lambda fn: fn.endswith(".ddl"), root[2]))

    # process root files
    database_creation_sqls = []
    init_sqls = []
    for filename in files:
        if filename.startswith("00_"):
            database_creation_sqls.append(os.path.join(directory, filename))
        else:
            init_sqls.append(os.path.join(directory, filename))
    # process schemas
    schemas_sqls = []
    if public_schema in schemas:
        # public folder has to go first
        schemas.remove(public_schema)
        schemas_sqls += process_schema(directory, public_schema, False)

    for schema in schemas:
        schemas_sqls += process_schema(directory, schema, True)

    # execute the collected Sqls
    if (len(database_creation_sqls) > 0) and create_db:
        logging.info("Creating database")
        db_conn_config = copy.copy(conn_config)
        db_conn_config.database = "postgres"
        sql = "\n".join(map(read_file, database_creation_sqls))
        execute_sql(db_conn_config, sql)

    if len(init_sqls) > 0:
        logging.info("Initializing the database")
        sql = "\n".join(map(read_file, init_sqls))
        execute_sql(conn_config, sql)
    if len(schemas_sqls) > 0:
        logging.info("Populating the schemas")
        sql = "\n".join(schemas_sqls)
        execute_sql(conn_config, sql)


def process_schema(base_dir: str, schema_name: str, expect_schema_creation: bool) -> list[str]:
    logging.info(f"  - schema '{schema_name}'")
    schema_dir = ensure_trailing_slash(base_dir + schema_name)
    schema_creation = []
    functions = []
    tables = []
    has_schema_creation = False
    files = os.listdir(schema_dir)
    files.sort()
    for input_file in files:
        if input_file == "_.ddl":
            schema_creation = [read_file(os.path.join(schema_dir, input_file))]
            has_schema_creation = True
        elif input_file.endswith(".sql"):
            functions.append(read_file(schema_dir + input_file))
        elif input_file.endswith(".ddl"):
            tables.append(read_file(schema_dir + input_file))

    if expect_schema_creation and not has_schema_creation:
        logging.warning(f"No schema creation found on path `{schema_dir}`")

    return schema_creation + functions + tables


if __name__ == '__main__':
    logging.basicConfig(
        level=logging.DEBUG, format="%(asctime)s,%(msecs)03d %(levelname)-8s [%(filename)s:%(lineno)s] %(message)s",
    )

    parsed_args = parse_args()

    db_conn_details = PostgresDBConn(
        username=parsed_args.username,
        password=parsed_args.password,
        host=parsed_args.host,
        database=parsed_args.dbname,
        port=parsed_args.port
    )

    process_dir(ensure_trailing_slash(parsed_args.dir), db_conn_details, parsed_args.create_db)
