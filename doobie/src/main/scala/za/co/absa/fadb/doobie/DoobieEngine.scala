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

import cats.Monad
import cats.effect.Async
import cats.implicits.toFlatMapOps
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.Read
import za.co.absa.fadb.DBEngine
import za.co.absa.fadb.status.StatusException

import scala.language.higherKinds

/**
 *  `DoobiePgEngine` is a class that extends `DBEngine` with `F` as the effect type.
 *  It uses Doobie's `Transactor[F]` to execute SQL queries.
 *
 *  `Async` is needed because Doobie requires it for non-blocking database operations.
 *
 *  @param transactor the Doobie transactor for executing SQL queries
 *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
 */
class DoobieEngine[F[_]: Async: Monad](val transactor: Transactor[F]) extends DBEngine[F] {

  /** The type of Doobie queries that produce `T` */
  type QueryType[R] = DoobieQuery[R]
//  type QueryWithStatusType[A, B, R] = DoobieQueryWithStatus[R]
  type QueryWithStatusType[R] = DoobieQueryWithStatus[R]

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

//  private def executeQueryWithStatusHandling[A, B, R](query: DoobieQueryWithStatus[R])(implicit readStatusWithDataR: Read[StatusWithData[R]]): F[Either[StatusException, R]] = {
  private def executeQueryWithStatusHandling[R](query: QueryWithStatusType[R])(implicit readStatusWithDataR: Read[StatusWithData[R]]): F[Either[StatusException, R]] = {
    query.fragment.query[StatusWithData[R]].unique.transact(transactor).map(query.getResultOrException)
  }

  /**
   *  Runs a Doobie query and returns the result as an `F[Seq[R]]`.
   *
   *  @param query the Doobie query to run
   *  @return the query result as an `F[Seq[R]]`
   */
  override def run[R](query: QueryType[R]): F[Seq[R]] =
    executeQuery(query)(query.readR)

//  override def fetchHeadWithStatusHandling[A, B, R](query: DoobieQueryWithStatus[R]): F[Either[StatusException, R]] = { // refactor in terms of run
  override def fetchHeadWithStatusHandling[R](query: QueryWithStatusType[R]): F[Either[StatusException, R]] = { // refactor in terms of run
    executeQueryWithStatusHandling(query)(query.readStatusWithDataR)
  }
}
