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
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.Read
import za.co.absa.fadb.DBEngine
import za.co.absa.fadb.exceptions.StatusException

import scala.language.higherKinds

/**
 *  [[DoobieEngine]] is a class that extends [[za.co.absa.fadb.DBEngine]] with `F` as the effect type.
 *  It uses [[doobie.Transactor]] to execute SQL queries.
 *
 *  [[cats.effect.Async]] is needed because Doobie requires it for non-blocking database operations.
 *
 *  @param transactor the [[doobie.Transactor]] for executing SQL queries
 *  @tparam F the effect type, which must have an [[cats.effect.Async]] instance
 */
class DoobieEngine[F[_]: Async](val transactor: Transactor[F]) extends DBEngine[F] {

  /** The type of Doobie queries that produce `T` */
  type QueryType[R] = DoobieQuery[R]
  type QueryWithStatusType[R] = DoobieQueryWithStatus[R]

  /**
   *  Executes a Doobie query and returns the result as an `F[Seq[R]]`.
   *
   *  @param query the Doobie query to execute
   *  @param readR the [[doobie.Read]] instance used to read the query result into `R`
   *  @return the query result as an `F[Seq[R]]`
   */
  private def executeQuery[R](query: QueryType[R])(implicit readR: Read[R]): F[Seq[R]] = {
    query.fragment.query[R].to[Seq].transact(transactor)
  }

  /**
   *  Executes a Doobie query and returns the result as an `F[Either[StatusException, R]]`.
   *
   *  @param query the Doobie query to execute
   *  @param readStatusWithDataR the [[doobie.Read]] instance used to read the query result into `StatusWithData[R]`
   *  @return the query result as an `F[Either[StatusException, R]]`
   */
  private def executeQueryWithStatus[R](
    query: QueryWithStatusType[R]
  )(implicit readStatusWithDataR: Read[StatusWithData[R]]): F[Either[StatusException, R]] = {
    // .unique returns a single value, raising an exception if there is not exactly one row returned
    // https://tpolecat.github.io/doobie/docs/04-Selecting.html
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

  /**
   *  Runs a Doobie query and returns the result as an `F[Either[StatusException, R]]`.
   *
   *  @param query the Doobie query to run
   *  @return the query result as an `F[Either[StatusException, R]]`
   */
  override def runWithStatus[R](query: QueryWithStatusType[R]): F[Either[StatusException, R]] = {
    executeQueryWithStatus(query)(query.readStatusWithDataR)
  }
}
