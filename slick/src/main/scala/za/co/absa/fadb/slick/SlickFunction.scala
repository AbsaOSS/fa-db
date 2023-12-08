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

import cats.implicits._
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.DBFunction.{DBMultipleResultFunction, DBOptionalResultFunction, DBSingleResultFunction}
import za.co.absa.fadb.exceptions.StatusException
import za.co.absa.fadb.{DBFunctionWithStatus, DBSchema, FunctionStatusWithData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
  protected def sql(values: I): SQLActionBuilder

  def fieldsToSelect: Seq[String]
}

private[slick] trait SlickFunction[I, R] extends SlickFunctionBase[I, R] {
  /**
   *  Generates a `SlickQuery[R]` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the `SlickQuery[R]` representing the SQL query
   */
  protected def query(values: I): SlickQuery[R] = new SlickQuery(sql(values), slickConverter)
}

private[slick] trait SlickFunctionWithStatus[I, R] extends SlickFunctionBase[I, R] {
  /**
   * Generates a `SlickQueryWithStatus[R]` representing the SQL query for the function with status support.
   *
   * @param status - the status to check
   * @return - Success or failure the status means
   */
  protected def query(values: I): SlickQueryWithStatus[R] =
    new SlickQueryWithStatus[R](sql(values), slickConverter, checkStatus)

  // Expected to be mixed in by an implementation of StatusHandling
  def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A]
}

object SlickFunction {
  /**
   *  Class for Slick DB functions with status support.
   */
  abstract class SlickSingleResultFunctionWithStatus[I, R](functionNameOverride: Option[String] = None)(implicit
    override val schema: DBSchema,
    DBEngine: SlickPgEngine
  ) extends DBFunctionWithStatus[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunctionWithStatus[I, R]

  /**
   *  Class for Slick DB functions with single result.
   */
  abstract class SlickSingleResultFunction[I, R](functionNameOverride: Option[String] = None)(implicit
    override val schema: DBSchema,
    DBEngine: SlickPgEngine
  ) extends DBSingleResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R]

  /**
   *  Class for Slick DB functions with multiple results.
   */
  abstract class SlickMultipleResultFunction[I, R](functionNameOverride: Option[String] = None)(implicit
    override val schema: DBSchema,
    DBEngine: SlickPgEngine
  ) extends DBMultipleResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R]

  /**
   *  Class for Slick DB functions with optional result.
   */
  abstract class SlickOptionalResultFunction[I, R](functionNameOverride: Option[String] = None)(implicit
    override val schema: DBSchema,
    DBEngine: SlickPgEngine
  ) extends DBOptionalResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R]
}
