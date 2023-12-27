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

import cats.effect.kernel.Async
import doobie.util.Read
import doobie.util.fragment.Fragment
import za.co.absa.fadb.DBFunction._
import za.co.absa.fadb.exceptions.StatusException
import za.co.absa.fadb.{DBFunctionWithStatus, DBSchema, DBStreamingFunction, FunctionStatusWithData}

import scala.language.higherKinds

private[doobiedb] trait DoobieFunctionBase[R] {
  /**
   *  The `Read[R]` instance used to read the query result into `R`.
   */
  implicit val readR: Read[R]
}

/**
 *  `DoobieFunction` provides support for executing database functions using Doobie.
 *
 *  @tparam I the input type of the function
 *  @tparam R the result type of the function
 */
private[doobie] trait DoobieFunction[I, R] extends DoobieFunctionBase[R] {
  /**
   *  Generates a Doobie `Fragment` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the Doobie `Fragment` representing the SQL query
   */
  def sql(values: I)(implicit read: Read[R]): Fragment

  /**
   *  Generates a `DoobieQuery[R]` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the `DoobieQuery[R]` representing the SQL query
   */
  protected def query(values: I): DoobieQuery[R] = new DoobieQuery[R](sql(values))
}

private[doobie] trait DoobieFunctionWithStatus[I, R] extends DoobieFunctionBase[R] {
  /**
   *  The `Read[StatusWithData[R]]` instance used to read the query result with status into `StatusWithData[R]`.
   */
  implicit def readStatusWithDataR(implicit readR: Read[R]): Read[StatusWithData[R]] = Read[(Int, String, R)].map {
    case (status, status_text, data) => StatusWithData(status, status_text, data)
  }

  /**
   *  Generates a Doobie `Fragment` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the Doobie `Fragment` representing the SQL query
   */
  def sql(values: I)(implicit read: Read[StatusWithData[R]]): Fragment

  /**
   *  Generates a `DoobieQueryWithStatus[R]` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the `DoobieQueryWithStatus[R]` representing the SQL query
   */
  protected def query(values: I): DoobieQueryWithStatus[R] = new DoobieQueryWithStatus[R](sql(values), checkStatus)

  // This is to be mixed in by an implementation of StatusHandling
  def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A]
}

/**
 *  `DoobieFunction` is an object that contains several abstract classes extending different types of database functions.
 *  These classes use Doobie's `Fragment` to represent SQL queries and `DoobieEngine` to execute them.
 */
object DoobieFunction {
  /**
   *  `DoobieSingleResultFunctionWithStatus` is an abstract class that extends `DBSingleResultFunctionWithStatus`
   *  with `DoobieEngine` as the engine type.
   *  It represents a database function that returns a single result with status.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobieEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @param readSelectWithStatus the `Read[StatusWithData[R]]` instance used to read the query result with status into `StatusWithData[R]`
   *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
   */
  abstract class DoobieSingleResultFunctionWithStatus[I, R, F[_]: Async](
    functionNameOverride: Option[String] = None
  )(implicit override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R],
    val readSelectWithStatus: Read[StatusWithData[R]]
  ) extends DBFunctionWithStatus[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunctionWithStatus[I, R]

  /**
   *  `DoobieSingleResultFunction` is an abstract class that extends `DBSingleResultFunction` with `DoobieEngine` as the engine type.
   *  It represents a database function that returns a single result.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobieEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
   */
  abstract class DoobieSingleResultFunction[I, R, F[_]: Async](functionNameOverride: Option[String] = None)(
    implicit override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBSingleResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R]

  /**
   *  `DoobieMultipleResultFunction` is an abstract class that extends `DBMultipleResultFunction`
   *  with `DoobieEngine` as the engine type.
   *  It represents a database function that returns multiple results.
   */
  abstract class DoobieMultipleResultFunction[I, R, F[_]: Async](functionNameOverride: Option[String] = None)(
    implicit override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBMultipleResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R]

  /**
   * `DoobieStreamingResultFunction` is an abstract class that extends `DBStreamingFunction`
   * with `DoobieStreamingEngine` as the engine type.
   * It represents a database function that returns a stream of results.
   */
  abstract class DoobieStreamingResultFunction[I, R, F[_]: Async](functionNameOverride: Option[String] = None)(
    implicit override val schema: DBSchema,
    val dbEngine: DoobieStreamingEngine[F],
    val readR: Read[R]
  ) extends DBStreamingFunction[I, R, DoobieStreamingEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R]

  /**
   *  `DoobieOptionalResultFunction` is an abstract class that extends `DBOptionalResultFunction`
   *  with `DoobieEngine` as the engine type.
   *  It represents a database function that returns an optional result.
   */
  abstract class DoobieOptionalResultFunction[I, R, F[_]: Async](functionNameOverride: Option[String] = None)(
    implicit override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBOptionalResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R]
}
