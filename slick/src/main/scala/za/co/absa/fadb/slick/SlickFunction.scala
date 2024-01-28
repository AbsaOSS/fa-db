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

package za.co.absa.fadb.slick

import cats.MonadError
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.DBFunction.{DBMultipleResultFunction, DBOptionalResultFunction, DBSingleResultFunction}
import za.co.absa.fadb.exceptions.StatusException
import za.co.absa.fadb.{DBFunctionWithStatus, DBSchema, FunctionStatusWithData}

import scala.concurrent.Future
import scala.language.higherKinds

/**
 *  Base class for Slick DB functions.
 *
 *  @tparam I the input type of the function
 *  @tparam R the result type of the function
 */
private[slick] trait SlickFunctionBase[I, R] {

  /**
   *  The `GetResult[R]` instance used to read the query result into `R`.
   */
  protected def slickConverter: GetResult[R]

  /**
   *  Generates a Slick `SQLActionBuilder` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the Slick `SQLActionBuilder` representing the SQL query
   */
  def sql(values: I): SQLActionBuilder

  /**
   * Generates a Slick `SQLActionBuilder` representing the SQL query for the function in the context of Future.
   * @param sqlActionBuilder Slick `SQLActionBuilder` representing the SQL query
   * @param ME MonadError instance for Future
   * @return the Slick `SQLActionBuilder` representing the SQL query wrapped in `Future`
   */
  protected final def meSql(
    sqlActionBuilder: => SQLActionBuilder
  )(implicit ME: MonadError[Future, Throwable]): Future[SQLActionBuilder] = {
    ME.catchNonFatal(sqlActionBuilder)
  }

  def fieldsToSelect: Seq[String]
}

private[slick] trait SlickFunction[I, R] extends SlickFunctionBase[I, R] {

  /**
   *  Generates a `SlickQuery[R]` representing the SQL query for the function.
   *  @param values the input values for the function
   *  @param ME MonadError instance for Future
   *  @return the `SlickQuery[R]` representing the SQL query wrapped in `Future`
   */
  protected def query(values: I)(implicit ME: MonadError[Future, Throwable]): Future[SlickQuery[R]] = {
    ME.flatMap(meSql(sql(values)))(sth => ME.pure(new SlickQuery[R](sth, slickConverter)))
  }
}

private[slick] trait SlickFunctionWithStatus[I, R] extends SlickFunctionBase[I, R] {

  /**
   *  Generates a `SlickQueryWithStatus[R]` representing the SQL query for the function with status support.
   *  @param values the input values for the function
   *  @param ME MonadError instance for Future
   *  @return the `SlickQueryWithStatus[R]` representing the SQL query wrapped in `Future`
   */
  protected def query(values: I)(implicit ME: MonadError[Future, Throwable]): Future[SlickQueryWithStatus[R]] = {
    ME.flatMap(meSql(sql(values)))(sth => ME.pure(new SlickQueryWithStatus[R](sth, slickConverter, checkStatus)))
  }

  // Expected to be mixed in by an implementation of StatusHandling
  def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A]
}

object SlickFunction {

  /**
   *  Class for Slick DB functions with status support.
   */
  abstract class SlickSingleResultFunctionWithStatus[I, R](
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    dBEngine: SlickPgEngine
  ) extends DBFunctionWithStatus[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunctionWithStatus[I, R]

  /**
   *  Class for Slick DB functions with single result.
   */
  abstract class SlickSingleResultFunction[I, R](
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    dBEngine: SlickPgEngine
  ) extends DBSingleResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R]

  /**
   *  Class for Slick DB functions with multiple results.
   */
  abstract class SlickMultipleResultFunction[I, R](
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    dBEngine: SlickPgEngine
  ) extends DBMultipleResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R]

  /**
   *  Class for Slick DB functions with optional result.
   */
  abstract class SlickOptionalResultFunction[I, R](
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    dBEngine: SlickPgEngine
  ) extends DBOptionalResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R]
}
