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

import cats.effect.Async
import doobie._
import doobie.implicits._
import za.co.absa.fadb.DBEngine

import scala.language.higherKinds

/**
 *  `DoobiePgEngine` is a class that extends `DBEngine` with `F` as the effect type.
 *  It uses Doobie's `Transactor[F]` to execute SQL queries.
 *
 *  `Async` is needed because Doobie requires it for non-blocking database operations.
 *  `Monad` is needed because it provides an interface for chaining operations together.
 *
 *  @param transactor the Doobie transactor for executing SQL queries
 *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
 */
class DoobiePgEngine[F[_]: Async](val transactor: Transactor[F]) extends DBEngine[F] {

  /** The type of Doobie queries that produce `T` */
  type QueryType[T] = DoobieQuery[T]

  /**
   *  Executes a Doobie query and returns the result as an `F[Seq[R]]`.
   *
   *  @param query the Doobie query to execute
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @return the query result as an `F[Seq[R]]`
   */
  private def executeQuery[R](query: QueryType[R])(implicit readR: Read[R]): F[Seq[R]] = {
    query.fragment.query[R].to[Seq].transact(transactor)
  }

  /**
   *  Runs a Doobie query and returns the result as an `F[Seq[R]]`.
   *
   *  @param query the Doobie query to run
   *  @return the query result as an `F[Seq[R]]`
   */
  override def run[R](query: QueryType[R]): F[Seq[R]] =
    executeQuery(query)(query.readR)
}
