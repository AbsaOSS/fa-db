/*
 * Copyright 2021 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.faDB.examples

//import cats.effect._
//import skunk._
//import skunk.implicits._
//import skunk.codec.all._
//import java.time.OffsetDateTime
//import natchez.Trace.Implicits.noop
//
//import java.time.OffsetDateTime
//
//object QueryExample extends IOApp {
//
//  // a source of sessions
//  val session: Resource[IO, Session[IO]] =
//    Session.single(
//      host = "localhost",
//      user = "postgres",
//      database = "skunk_db",
//      password = Some("postgres")
//    )
//
//  // a data model
//  case class Country(name: String, code: String, population: Int)
//
//  // a simple query
//  val simple: Query[Void, OffsetDateTime] = {
//    val sql = sql"select current_timestamp"
//      .query(timestamptz)
//  }
//
//  // an extended query
//  val extended: Query[String, Country] =
//    sql"""
//      SELECT name, code, population
//      FROM   country
//      WHERE  name like $text
//    """.query(varchar ~ bpchar(3) ~ int4)
//      .gmap[Country]
//
//  // run our simple query
//  def doSimple(s: Session[IO]): IO[Unit] =
//    for {
//      ts <- s.unique(simple) // we expect exactly one row
//      _ <- IO.println(s"timestamp is $ts")
//    } yield ()
//
//  // run our extended query
//  def doExtended(s: Session[IO]): IO[Unit] =
//    s.prepare(extended).use { ps =>
//      ps.stream("U%", 32)
//        .evalMap(c => IO.println(c))
//        .compile
//        .drain
//    }
//
//  // our entry point
//  def run(args: List[String]): IO[ExitCode] =
//    session.use { s =>
//      for {
//        _ <- doSimple(s)
//        _ <- doExtended(s)
//      } yield ExitCode.Success
//    }
//
//}
