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
- [How to generate code coverage report](#how-to-generate-code-coverage-report)
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


### Usage

#### Sbt

```scala
libraryDependencies ++= Seq(
  "za.co.absa.fa-db" %% "core"  % "X.Y.Z",
  "za.co.absa.fa-db" %% "slick" % "X.Y.Z"
)
```

#### Maven

##### Scala 2.11

Modules:
* Core [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/core_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/core_2.11)
* Slick [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/slick_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/slick_2.11)

```xml
<dependency>
   <groupId>za.co.absa.fa-db</groupId>
   <artifactId>core_2.11</artifactId>
   <version>${latest_version}</version>
</dependency>
<dependency>
<groupId>za.co.absa.fa-db</groupId>
<artifactId>slick_2.11</artifactId>
<version>${latest_version}</version>
</dependency>
```

### Scala 2.12 
Modules:
* Core [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/core_2.12)
* Slick [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/slick_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa.fa-db/slick_2.12)

```xml
<dependency>
   <groupId>za.co.absa.fa-db</groupId>
   <artifactId>core_2.12</artifactId>
   <version>${latest_version}</version>
</dependency>
<dependency>
    <groupId>za.co.absa.fa-db</groupId>
    <artifactId>slick_2.12</artifactId>
    <version>${latest_version}</version>
</dependency>
```

## Concepts

### Status codes

Text about status codes returned from the database function can be found [here](core/src/main/scala/za/co/absa/fadb/status/README.md).

## How to generate code coverage report
```sbt
sbt jacoco
```
Code coverage will be generated on path:
```
{project-root}/fa-db/{module}/target/scala-{scala_version}/jacoco/report/html
```

## How to Release

Please see [this file](RELEASE.md) for more details.