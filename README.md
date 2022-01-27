# fa-DB
## _Functional Access to Database_

This library is a less traditional way how to facilitate data between an application and an SQL Database.

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
* Separation of the domains while keeping each part to do what they are good for:
    * the application parsing and utilizing the data
    * the DB storing and retrieving the data effectively
* Better data security and consistency protection

---

Currently the library is developed with Postgres as the target DB. But the approach is applicable to any DB supporting stored procedure/functions – Oracle, MS-SQL, ...

