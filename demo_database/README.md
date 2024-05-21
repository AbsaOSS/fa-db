## About

This module implements a simple database with many types of objects (tables, functions, data insertions, and more)
that will be used in integration tests in these modules:
* `doobie/src/it/`
* `slick/src/it/`

## Deployment

How to set up database for local testing

### Using Docker

```zsh
# start up postgres docker container (optional; instead you can create movies on your local postgres instance)
docker run --name=movies -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=movies -p 5432:5432 -d postgres:16

# migrate scripts
sbt flywayMigrate

# kill & remove docker container (optional; only if using dockerized postgres instance)
docker kill aul_db
docker rm aul_db
```

### Using local postgres instance
- create database `movies`
- create required extension in file `V1.1.1__01_add_extensions.ddl`

- migrate scripts
```zsh
sbt flywayMigrate
```

In case some structures are already present in the database, you can use
```zsh
sbt flywayClean 
```
to remove them or
```zsh
sbt flywayBaseline 
```
to set the current state as the baseline.
