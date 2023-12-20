///*
// * Copyright 2022 ABSA Group Limited
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package za.co.absa.fadb.doobiedb
//
//import cats.effect.IO
//import cats.effect.unsafe.implicits.global
//import doobie.implicits.toSqlInterpolator
//import doobie.util.Read
//import doobie.util.fragment.Fragment
//import org.scalatest.funsuite.AnyFunSuite
//import za.co.absa.fadb.DBSchema
//import za.co.absa.fadb.doobiedb.DoobieFunction.{DoobieSingleResultFunction, DoobieSingleResultFunctionWithStatus}
//import za.co.absa.fadb.status.handling.implementations.StandardStatusHandling
//
//class DatesTimesTest extends AnyFunSuite with DoobieTest {
//
//  // these imports are needed
//  import doobie.postgres._
//  import doobie.postgres.implicits._
//  import doobie.implicits.javasql._ // java.sql
//
//  // Provides mappings for java.time.Instant
////  import doobie.implicits.legacy.instant._
//
//  // Provides mappings for java.time.LocalDate
////  import doobie.implicits.legacy.localdate._
//
//
//  // this import is needed for the implicit Meta[java.time.ZonedDateTime]
//  import za.co.absa.fadb.doobiedb.postgres.implicits._
//
//  case class DatesTimes(
//                               offsetDateTime: java.time.OffsetDateTime,
//                               instant: java.time.Instant,
//                               zonedDateTime: java.time.ZonedDateTime,
//                               localDateTime: java.time.LocalDateTime,
//                               localDate: java.time.LocalDate,
//                               localTime: java.time.LocalTime, //conversion to class java.time.LocalTime from timestamptz not supported
//                               sqlDate: java.sql.Date,
//                               sqlTime: java.sql.Time,
//                               sqlTimestamp: java.sql.Timestamp,
//                               utilDate: java.util.Date
//                             )
//
//  class GetAllDateTimeTypes(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
//      extends DoobieSingleResultFunction[Int, DatesTimes, IO] {
//
//    override def sql(values: Int)(implicit read: Read[DatesTimes]): Fragment =
//      sql"SELECT * FROM ${Fragment.const(functionName)}($values)"
//  }
//
//  class InsertDatesTimes(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
//      extends DoobieSingleResultFunctionWithStatus[DatesTimes, Int, IO] with StandardStatusHandling {
//
//    override def sql(values: DatesTimes)(implicit read: Read[StatusWithData[Int]]): Fragment =
//      sql"""
//           SELECT * FROM ${Fragment.const(functionName)}(
//            ${values.offsetDateTime},
//            ${values.instant},
//            ${values.zonedDateTime},
//            ${values.localDateTime},
//            ${values.localDate},
//            ${values.localTime},
//            ${values.sqlDate},
//            ${values.sqlTime},
//            ${values.sqlTimestamp},
//            ${values.utilDate}
//           )
//        """
//  }
//
//  private val getAllDateTimeTypes = new GetAllDateTimeTypes()(Runs, new DoobieEngine(transactor))
//  private val insertDatesTimes = new InsertDatesTimes()(Runs, new DoobieEngine(transactor))
//
//  test("Reading different date/time types from the database") {
//    val offsetDateTime = java.time.OffsetDateTime.parse("2004-10-19T08:23:54Z")
//    val instant = java.time.Instant.parse("2004-10-19T08:23:54Z")
//    val zonedDateTime = java.time.ZonedDateTime.parse("2004-10-19T10:23:54+02:00[Europe/Prague]")
//    val localDateTime = java.time.LocalDateTime.parse("2004-10-19T10:23:54")
//    val localDate = java.time.LocalDate.parse("2004-10-19")
//    val localTime = java.time.LocalTime.parse("10:23:54")
//    val sqlDate = java.sql.Date.valueOf("2004-10-19")
//    val sqlTime = java.sql.Time.valueOf("10:23:54")
//    val sqlTimestamp = java.sql.Timestamp.valueOf("2004-10-19 10:23:54.0")
//    val utilDate = new java.util.Date(sqlDate.getTime)
//
//    val expectedDatesTimes = DatesTimes(
//      offsetDateTime,
//      instant,
//      zonedDateTime,
//      localDateTime,
//      localDate,
//      localTime,
//      sqlDate,
//      sqlTime,
//      sqlTimestamp,
//      utilDate
//    )
//    val result = getAllDateTimeTypes(1).unsafeRunSync()
//    assert(expectedDatesTimes == result)
//  }
//
//  test("Writing different date/time types to the database") {
//    val offsetDateTime = java.time.OffsetDateTime.parse("2004-10-19T08:23:54Z")
//    val instant = java.time.Instant.parse("2004-10-19T08:23:54Z")
//    val zonedDateTime = java.time.ZonedDateTime.parse("2004-10-19T10:23:54+02:00[Europe/Prague]")
//    val localDateTime = java.time.LocalDateTime.parse("2004-10-19T10:23:54")
//    val localDate = java.time.LocalDate.parse("2004-10-19")
//    val localTime = java.time.LocalTime.parse("10:23:54")
//    val sqlDate = java.sql.Date.valueOf("2004-10-19")
//    val sqlTime = java.sql.Time.valueOf("10:23:54")
//    val sqlTimestamp = java.sql.Timestamp.valueOf("2004-10-19 10:23:54.0")
//    val utilDate = new java.util.Date(sqlDate.getTime)
//
//    val datesTimes = DatesTimes(
//      offsetDateTime,
//      instant,
//      zonedDateTime,
//      localDateTime,
//      localDate,
//      localTime,
//      sqlDate,
//      sqlTime,
//      sqlTimestamp,
//      utilDate
//    )
//
//    val result = insertDatesTimes(datesTimes).unsafeRunSync()
//    assert(result.isRight)
//  }
//
//}
