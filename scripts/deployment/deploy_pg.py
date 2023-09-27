#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import dataclasses
import argparse
import logging
import os
import psycopg2
import copy


FUNCTION_FILES = ".sql"
DDL_FILES = ".ddl"
DB_CREATION_FILES = "00_"
SCHEMA_CREATION_FILE = "_" + DDL_FILES



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
        default=5432,
        type=int,
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
    try:
        conn.autocommit = True
        cursor = conn.cursor()

        cursor.execute(sql)

        conn.commit()
    finally:
        conn.close()


def read_file(file_name: str) -> str:
    logging.debug(f"    - reading file `{file_name}`")
    with open(file_name, "r") as file:
        return file.read()


def process_dir(directory: str, conn_config: PostgresDBConn, create_db: bool) -> None:
    logging.info(f"Picking up source files from directory `{directory}`")
    public_schema = "public"
    root_dir_content = next(os.walk(directory))
    schemas = root_dir_content[1] # schemas equals directories
    root_ddl_files = list(filter(lambda fn: fn.endswith(DDL_FILES), root_dir_content[2]))

    # process root files
    database_creation_files = []
    database_init_files = []
    for filename in root_ddl_files:
        if filename.startswith(DB_CREATION_FILES):
            database_creation_files.append(os.path.join(directory, filename))
        else:
            database_init_files.append(os.path.join(directory, filename))
    # process schemas
    schemas_sqls = []
    if public_schema in schemas:
        # public folder has to go first
        schemas.remove(public_schema)
        schemas_sqls += process_schema(directory, public_schema, False)

    for schema in schemas:
        schemas_sqls += process_schema(directory, schema, True)

    # execute the collected Sqls
    if (len(database_creation_files) > 0) and create_db:
        logging.info("Creating database...")
        db_conn_config = copy.copy(conn_config)
        db_conn_config.database = "postgres"
        database_creation_sql = "\n".join(map(read_file, database_creation_files))
        execute_sql(db_conn_config, database_creation_sql)

    if len(database_init_files) > 0:
        logging.info("Initializing the database...")
        database_init_sql = "\n".join(map(read_file, database_init_files))
        execute_sql(conn_config, database_init_sql)
    if len(schemas_sqls) > 0:
        logging.info("Populating the schemas...")
        schemas_population_sql = "\n".join(schemas_sqls)
        execute_sql(conn_config, schemas_population_sql)
    logging.info("... all done.")


def process_schema(base_dir: str, schema_name: str, expect_schema_creation: bool) -> list[str]:
    logging.info(f"  - schema '{schema_name}'")
    schema_dir = os.path.join(base_dir, schema_name)
    schema_creation = []
    functions_creation_sqls = []
    tables_creation_sqls = []
    has_schema_creation = False
    files = os.listdir(schema_dir)
    files.sort()
    for input_file in files:
        if input_file == SCHEMA_CREATION_FILE:
            schema_creation = [read_file(os.path.join(schema_dir, input_file))]
            has_schema_creation = True
        elif input_file.endswith(FUNCTION_FILES):
            functions_creation_sqls.append(read_file(os.path.join(schema_dir, input_file)))
        elif input_file.endswith(DDL_FILES):
            tables_creation_sqls.append(read_file(os.path.join(schema_dir, input_file)))

    if expect_schema_creation and not has_schema_creation:
        logging.warning(f"No schema creation found on path `{schema_dir}`")

    return schema_creation + functions_creation_sqls + tables_creation_sqls


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

    process_dir(parsed_args.dir, db_conn_details, parsed_args.create_db)
