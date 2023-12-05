/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.fadb.doobie

import cats.effect.IO
import doobie._
import doobie.implicits._
import za.co.absa.fadb.DBEngine

/**
 *  `DoobiePgEngine` is a class that extends `DBEngine` with `IO` as the effect type.
 *  It uses Doobie's `Transactor[IO]` to execute SQL queries.
 *
 *  @param transactor the Doobie transactor for executing SQL queries
 */
class DoobiePgEngine(val transactor: Transactor[IO]) extends DBEngine[IO] {

  /** The type of Doobie queries that produce `T` */
  type QueryType[T] = DoobieQuery[T]

  /**
   *  Executes a Doobie query and returns the result as an `IO[Seq[R]]`.
   *
   *  @param query the Doobie query to execute
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @return the query result as an `IO[Seq[R]]`
   */
  private def executeQuery[R](query: QueryType[R])(implicit readR: Read[R]): IO[Seq[R]] = {
    query.fragment.query[R].to[Seq].transact(transactor)
  }

  /**
   *  Runs a Doobie query and returns the result as an `IO[Seq[R]]`.
   *
   *  @param query the Doobie query to run
   *  @return the query result as an `IO[Seq[R]]`
   */
  override def run[R](query: QueryType[R]): IO[Seq[R]] =
    executeQuery(query)(query.readR)
}
