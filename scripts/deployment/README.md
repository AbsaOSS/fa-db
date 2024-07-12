# Deployment script

This _Python_ script is there to help deploy DB objects at a clear Postgres database or database server.

Expectations are that the source files are placed in a directory structure of following properties:
* database creation script is stored in source root, starts with `00_` and has the extension of `.ddl` (those files are skipped unless a database creation switch is provided to the deployment script)
* other required systemic changes (users, extensions) are stored in files in root, their extension is `.ddl` and are processed alphabetically
* it's recommended these to be written in re-executable (idempotent) way, because they might affect the whole server not just the database
* objects belonging to certain schemas are in a given schema-related folders (convention: folder name equals schema name)
* schema creation SQL is stored in file `_.ddl`
* DB functions are stored in files with `.sql` extension (conventions: file name equals function name)
* tables are stored in files with `.ddl` extension (conventions: file name equals table name)
* processing order is: schema -> functions (alphabetically) -> tables (alphabetically)

The schema structure should look something like this:
```
my_basic_schema/
    - _.ddl
    - my_data_table.ddl
    - my_function1.sql
    - my_function2.sql
public/
    - some_general_function.sql
random_data/
    - _.ddl
    - another_function.sql
    - random_data_table.ddl
00_databases.ddl
01_users.ddl
02_my_own_db.ddl        
```
