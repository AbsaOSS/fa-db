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

package za.co.absa.fadb

import cats.Monad
import cats.implicits.toFunctorOps
import za.co.absa.fadb.status.StatusException

import scala.language.higherKinds

/**
  * A basis to represent a database executor
  */
abstract class DBEngine[F[_]: Monad] {

  /**
    * A type representing the (SQL) query within the engine
    * @tparam T - the return type of the query
    */
  type QueryType[T] <: Query[T]
//  type QueryWithStatusType[A, B, R] <: QueryWithStatus[A, B, R]
  type QueryWithStatusType[R] <: QueryWithStatus[_, _, R]

  /**
    * The actual query executioner of the queries of the engine
    * @param query  - the query to execute
    * @tparam R     - return the of the query
    * @return       - sequence of the results of database query
    */
  protected def run[R](query: QueryType[R]): F[Seq[R]]

//  def fetchHeadWithStatusHandling[A, B, R](query: QueryWithStatusType[A, B, R]): F[Either[StatusException, R]]
  def fetchHeadWithStatusHandling[R](query: QueryWithStatusType[R]): F[Either[StatusException, R]]

  /**
    * Public method to execute when query is expected to return multiple results
    * @param query  - the query to execute
    * @tparam R     - return the of the query
    * @return       - sequence of the results of database query
    */
  def fetchAll[R](query: QueryType[R]): F[Seq[R]] = run(query)

  /**
    * Public method to execute when query is expected to return exactly one row
    * @param query  - the query to execute
    * @tparam R     - return the of the query
    * @return       - sequence of the results of database query
    */
  def fetchHead[R](query: QueryType[R]): F[R] = {
    run(query).map(_.head)
  }

  /**
    * Public method to execute when query is expected to return one or no results
    * @param query  - the query to execute
    * @tparam R     - return the of the query
    * @return       - sequence of the results of database query
    */

  def fetchHeadOption[R](query: QueryType[R]): F[Option[R]] = {
    run(query).map(_.headOption)
  }
}

