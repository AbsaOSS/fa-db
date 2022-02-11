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
//import natchez.Trace.Implicits.noop                          // (1)                          // (1)
//
//object Hello extends IOApp {
//
//  val session: Resource[IO, Session[IO]] =
//    Session.single(                                          // (2)
//      host     = "localhost",
//      port     = 5432,
//      user     = "jimmy",
//      database = "world",
//      password = Some("banana")
//    )
//
//  def run(args: List[String]): IO[ExitCode] =
//    session.use { s =>                                       // (3)
//      for {
//        d <- s.unique(sql"select current_date".query(date))  // (4)
//        _ <- IO.println(s"The current date is $d.")
//      } yield ExitCode.Success
//    }
//
//}
