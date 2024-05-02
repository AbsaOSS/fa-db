# fa-db
## _Functional Access to Database_

### Build Status

[![Build](https://github.com/AbsaOSS/fa-db/workflows/Build/badge.svg)](https://github.com/AbsaOSS/fa-db/actions)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/core_2.11/badge.svg)](https://search.maven.org/search?q=g:za.co.absa.fa-db)

___

<!-- toc -->
- [What is fa-db](#what-is-fa-db)
- [Usage](#usage)
- [Concepts](#concepts)
- [Slick module](#slick-module)
- [Testing](#testing)
- [How to Release](#how-to-release)
<!-- tocstop -->

## What is fa-db

This library is a less traditional way to facilitate data between an application and an SQL Database.

Traditionally application directly applies SQL queries or use some ORM framework. While the first approach mixes two
rather different domain languages within one source, the second too often fails in case of more complicated queries and 
table relations.

---

This library offers a different approach.

The idea is that the application transfers data to and from using database stored procedures/functions (from here on
referenced in this library as _DB functions_). This establishes a stable contract between the DB and the application. To 
emphasize – the data are both **read** and **written** to/from DB using _DB functions_.

**The purpose of the library then is to facilitate an easy and natural – meaning Scala style – call of the _DB functions_ 
within the application.**

<u>Benefits:</u>
* Stable contract between the application and the DB
* Early locking of the data model
* Separation of the domains while keeping each part doing what they are good for:
    * the application parsing and utilizing the data
    * the DB storing and retrieving the data effectively
* Better data security and consistency protection

---

Currently, the library is developed with Postgres as the target DB. But the approach is applicable to any DB supporting stored procedure/functions – Oracle, MS-SQL, ...


## Usage

#### Sbt

Import one of the two available module at the moment. Slick module works with Scala Futures. Doobie module works with any effect type (typically IO or ZIO) provided cats effect's Async instance is available.

```scala
libraryDependencies *= "za.co.absa.fa-db" %% "slick"  % "X.Y.Z"
libraryDependencies *= "za.co.absa.fa-db" %% "doobie"  % "X.Y.Z"
```

#### Maven

##### Scala 2.12

Modules:
* Core [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/core_2.12)
* Slick [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/slick_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/slick_2.12)
* Doobie [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/doobie_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/doobie_2.12)

```xml
<dependency>
    <groupId>za.co.absa.fa-db</groupId>
    <artifactId>slick_2.12</artifactId>
    <version>${latest_version}</version>
</dependency>
<dependency>
    <groupId>za.co.absa.fa-db</groupId>
    <artifactId>doobie_2.12</artifactId>
    <version>${latest_version}</version>
</dependency>
```

### Scala 2.13
Modules:
* Core [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/core_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/core_2.13)
* Slick [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/slick_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/slick_2.13)
* Doobie [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/doobie_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/doobie_2.13)

```xml
<dependency>
    <groupId>za.co.absa.fa-db</groupId>
    <artifactId>slick_2.13</artifactId>
    <version>${latest_version}</version>
</dependency>
<dependency>
    <groupId>za.co.absa.fa-db</groupId>
    <artifactId>doobie_2.13</artifactId>
    <version>${latest_version}</version>
</dependency>
```

## Concepts

### Status codes

Text about status codes returned from the database function can be found [here](core/src/main/scala/za/co/absa/fadb/status/README.md).


## Slick module

As the name suggests it runs on [Slick library](https://github.com/slick/slick) and also brings in the [Slickpg library](https://github.com/tminglei/slick-pg/) for extended Postgres type support.

It brings:

* `class SlickPgEngine` - implementation of _Core_'s `DBEngine` executing the queries via Slick
* `class SlickSingleResultFunction` - abstract class for DB functions returning single result
* `class SlickMultipleResultFunction` - abstract class for DB functions returning sequence of results
* `class SlickOptionalResultFunction` - abstract class for DB functions returning optional result
* `class SlickSingleResultFunctionWithStatus` - abstract class for DB functions with status handling; it requires an implementation of `StatusHandling` to be mixed-in (`StandardStatusHandling` available out-of-the-box)
* `trait FaDbPostgresProfile` - to bring support for Postgres and its extended data types in one class (except JSON, as there are multiple implementations for this data type in _Slick-Pg_)
* `object FaDbPostgresProfile` - instance of the above trait for direct use

#### Known issues

When getting result from `PositionedResult` for these types `HStore` -> `Option[Map[String, String]]` and 
`macaddr` -> `MacAddrString` type inference doesn't work well.
So instead of:
```scala
val pr: PositionedResult = ???
val hStore: Option[Map[String, String]] = pr.<<
val macAddr: Option[MacAddrString] = pr.<<
```

explicit extraction needs to be used:
```scala
val pr: PositionedResult = ???
val hStore: Option[Map[String, String]] = pr.nextHStoreOption
val macAddr: Option[MacAddrString] = pr.nextMacAddrOption
```

## Doobie module

As the name suggests it runs on [Doobie library](https://tpolecat.github.io/doobie/). The main benefit of the module is that it allows to use any effect type (typically IO or ZIO) therefore is more suitable for functional programming. It also brings in the [Doobie-Postgres library](https://tpolecat.github.io/doobie/docs/14-PostgreSQL.html) for extended Postgres type support.

It brings:

* `class DoobieEngine` - implementation of _Core_'s `DBEngine` executing the queries via Doobie. The class is type parameterized with the effect type.
* `class DoobieSingleResultFunction` - abstract class for DB functions returning single result
* `class DoobieMultipleResultFunction` - abstract class for DB functions returning sequence of results
* `class DoobieOptionalResultFunction` - abstract class for DB functions returning optional result
* `class DoobieSingleResultFunctionWithStatus` - abstract class for DB functions with status handling; it requires an implementation of `StatusHandling` to be mixed-in (`StandardStatusHandling` available out-of-the-box)

Since Doobie also interoperates with ZIO, there is an example of how a database connection can be properly established within a ZIO application. Please see [this file](doobie/zio-setup.md) for more details.

## Testing

### How to generate unit tests code coverage report

```sbt
sbt jacoco
```
Note: this command will start all tests in the project.

Code coverage will be generated on path:

```
{project-root}/fa-db/{module}/target/scala-{scala_version}/jacoco/report/html
```

### Integration tests

There are now integration tests as part of the project (at the time of writing they are in 
the _Slick_ and _Doobie_ modules).

For the tests to work properly a running Postgres instance is needed as well as all DB objects must be placed on the DB.
We automated this process, see [demo_database/README.md](https://github.com/AbsaOSS/fa-db/tree/master/demo_database#readme) for more details.

How to execute the `unit` tests only:
```sbt
sbt test
```
How to execute the `integration` tests only:
```sbt
sbt testIT
```
How to execute the `all` tests only:
```sbt
sbt testAll
```

## How to Release

Please see [this file](RELEASE.md) for more details.
