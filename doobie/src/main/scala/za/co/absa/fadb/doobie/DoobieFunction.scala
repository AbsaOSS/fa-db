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
import cats.effect.kernel.Async
import cats.implicits.toFlatMapOps
import doobie.util.Read
import doobie.util.fragment.Fragment
import za.co.absa.fadb.DBFunction._
import za.co.absa.fadb.{DBFunctionWithStatusHandling, DBSchema}
import za.co.absa.fadb.doobie.Results.{FailedResult, ResultWithStatus, SuccessfulResult}
import za.co.absa.fadb.status.{FunctionStatus, StatusException}
import za.co.absa.fadb.status.handling.StatusHandling

import scala.language.higherKinds
import scala.util.{Failure, Success}

/**
 *  `DoobieFunction` provides support for executing database functions using Doobie.
 *
 *  @tparam I the input type of the function
 *  @tparam R the result type of the function
 */
private[doobie] trait DoobieFunction[I, R] {

  /**
   *  The `Read[R]` instance used to read the query result into `R`.
   */
  implicit val readR: Read[R]

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

/**
 *  `DoobieFunctionWithStatusSupport` provides support for executing database functions with status handling using Doobie.
 *
 *  @tparam I the input type of the function
 *  @tparam R the result type of the function
 */
//private[doobie] trait DoobieFunctionWithStatusSupport[I, R] extends StatusHandling {
//
//  /**
//   *  The `Read[R]` instance used to read the query result into `R`.
//   */
//  implicit val readR: Read[R]
//
//  /**
//   *  The `Read[(Int, String, R)]` instance used to read the query result with status into `(Int, String, R)`.
//   */
//  implicit val readSelectWithStatus: Read[(Int, String, R)]
//
//  /**
//   *  Generates a Doobie `Fragment` representing the SQL query for the function.
//   *
//   *  @param values the input values for the function
//   *  @return the Doobie `Fragment` representing the SQL query
//   */
//  def sql(values: I)(implicit read: Read[R]): Fragment
//
//  /**
//   *  Generates a `DoobieQuery[(Int, String, R)]` representing the SQL query for the function with status.
//   *
//   *  @param values the input values for the function
//   *  @return the `DoobieQuery[(Int, String, R)]` representing the SQL query with status
//   */
//  protected def query(values: I): DoobieQuery[(Int, String, R)] = new DoobieQuery[(Int, String, R)](sql(values))
//}

private[doobie] trait DoobieFunctionWithStandardStatusHandling[I, R] {
  implicit val readR: Read[R]
  implicit def readStatusWithDataR[R](implicit readR: Read[R]): Read[StatusWithData[R]] = Read[(Int, String, R)].map {
    case (status, status_text, data) => StatusWithData(status, status_text, data)
  }
  def sql(values: I)(implicit read: Read[StatusWithData[R]]): Fragment

}

/**
 *  `DoobieFunction` is an object that contains several abstract classes extending different types of database functions.
 *  These classes use Doobie's `Fragment` to represent SQL queries and `DoobiePgEngine` to execute them.
 */
object DoobieFunction {

  abstract class DoobieSingleResultFunctionWithStandardStatusHandling[I, R, F[_]: Async: Monad](
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R],
    val readSelectWithStatus: Read[StatusWithData[R]]
  ) extends DBSingleResultWithStatusHandlingFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunctionWithStandardStatusHandling[I, R] {

    protected def query(values: I): dbEngine.QueryWithStatusType[R] = new DoobieQueryWithStatus[R](sql(values))
  }

  /**
   *  `DoobieSingleResultFunction` is an abstract class that extends `DBSingleResultFunction` with `DoobiePgEngine` as the engine type.
   *  It represents a database function that returns a single result.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobiePgEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
   */
  abstract class DoobieSingleResultFunction[I, R, F[_]: Async: Monad](functionNameOverride: Option[String] = None)(
    implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBSingleResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R]

  /**
   *  `DoobieMultipleResultFunction` is an abstract class that extends `DBMultipleResultFunction` with `DoobiePgEngine` as the engine type.
   *  It represents a database function that returns multiple results.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobiePgEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
   */
  abstract class DoobieMultipleResultFunction[I, R, F[_]: Async: Monad](functionNameOverride: Option[String] = None)(
    implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBMultipleResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R]

  /**
   *  `DoobieOptionalResultFunction` is an abstract class that extends `DBOptionalResultFunction` with `DoobiePgEngine` as the engine type.
   *  It represents a database function that returns an optional result.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobiePgEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
   */
  abstract class DoobieOptionalResultFunction[I, R, F[_]: Async: Monad](functionNameOverride: Option[String] = None)(
    implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBOptionalResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R]

  /**
   *  `DoobieSingleResultFunctionWithStatusSupport` is an abstract class that extends `DBSingleResultFunction` with `DoobiePgEngine` as the engine type.
   *  It represents a database function that returns a single result with status.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobiePgEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @param readSelectWithStatus the `Read[(Int, String, R)]` instance used to read the query result with status into `(Int, String, R)`
   *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
   */
//  abstract class DoobieSingleResultFunctionWithStatusSupport[I, R, F[_]: Async: Monad](
//    functionNameOverride: Option[String] = None
//  )(implicit
//    override val schema: DBSchema,
//    val dbEngine: DoobieEngine[F],
//    val readR: Read[R],
//    val readSelectWithStatus: Read[(Int, String, R)]
//  ) extends DBSingleResultFunction[I, (Int, String, R), DoobieEngine[F], F](functionNameOverride)
//      with DoobieFunctionWithStatusSupport[I, R] {
//
//    /**
//     *  Executes the function with the given input values and returns the result with status.
//     *
//     *  @param values the input values for the function
//     *  @param monad the `Monad` instance used to chain operations together
//     *  @return the result with status
//     */
//    def applyWithStatus(values: I)(implicit monad: Monad[F]): F[ResultWithStatus[R]] = {
//      super.apply(values).flatMap { case (status, statusText, result) =>
//        checkStatus(status, statusText) match {
//          case Success(_) => monad.pure(Right(SuccessfulResult[R](FunctionStatus(status, statusText), result)))
//          case Failure(e) => monad.pure(Left(FailedResult(FunctionStatus(status, statusText), e)))
//        }
//      }
//    }
//  }

}
