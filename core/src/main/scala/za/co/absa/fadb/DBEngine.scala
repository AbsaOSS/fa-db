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
import za.co.absa.fadb.status.ExceptionOrStatusWithDataRow

import scala.language.higherKinds

/**
 *  `DBEngine` is an abstract class that represents a database engine.
 *  It provides methods to execute queries and fetch results from a database.
 *  @tparam F - The type of the context in which the database queries are executed.
 */
abstract class DBEngine[F[_]: Monad] {

  /**
   *  A type representing the (SQL) query within the engine
   *  @tparam R - the return type of the query
   */
  type QueryType[R] <: Query[R]
  type QueryWithStatusType[R] <: QueryWithStatus[_, _, R]

  /**
   *  The actual query executioner of the queries of the engine
   *
   *  Two methods provided, one for dealing with query of type with no status, and the other for status-provided queries.
   *
   *  @param query  - the query to execute
   *  @tparam R     - return type of the query
   *  @return       - sequence of the results of database query
   */
  protected def run[R](query: QueryType[R]): F[Seq[R]]
  protected def runWithStatus[R](query: QueryWithStatusType[R]): F[Seq[ExceptionOrStatusWithDataRow[R]]]

  /**
   *  Public method to execute when query is expected to return multiple results
   *
   *  Two methods provided, one for dealing with query of type with no status, and the other for status-provided queries.
   *
   *  @param query  - the query to execute
   *  @tparam R     - return type of the query
   *  @return       - sequence of the results of database query
   */
  def fetchAll[R](query: QueryType[R]): F[Seq[R]] = run(query)
  def fetchAllWithStatus[R](query: QueryWithStatusType[R]): F[Seq[ExceptionOrStatusWithDataRow[R]]] =
    runWithStatus(query)

  /**
   *  Public method to execute when query is expected to return exactly one row
   *
   *  Two methods provided, one for dealing with query of type with no status, and the other for status-provided queries.
   *
   *  @param query  - the query to execute
   *  @tparam R     - return type of the query
   *  @return       - sequence of the results of database query
   */
  def fetchHead[R](query: QueryType[R]): F[R] = run(query).map(_.head)
  def fetchHeadWithStatus[R](query: QueryWithStatusType[R]): F[ExceptionOrStatusWithDataRow[R]] =
    runWithStatus(query).map(_.head)

  /**
   *  Public method to execute when query is expected to return one or no results
   *
   *  Two methods provided, one for dealing with query of type with no status, and the other for status-provided queries.
   *
   *  @param query  - the query to execute
   *  @tparam R     - return type of the query
   *  @return       - sequence of the results of database query
   */
  def fetchHeadOption[R](query: QueryType[R]): F[Option[R]] = run(query).map(_.headOption)
  def fetchHeadOptionWithStatus[R](query: QueryWithStatusType[R]): F[Option[ExceptionOrStatusWithDataRow[R]]] =
    runWithStatus(query).map(_.headOption)
}
