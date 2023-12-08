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
import za.co.absa.fadb.exceptions.StatusException
import za.co.absa.fadb.status.handling.QueryStatusHandling

import scala.language.higherKinds

/**
 * `DBFunction` is an abstract class that represents a database function.
 * @param functionNameOverride - Optional parameter to override the class name if it does not match the database function name.
 * @param schema - The schema the function belongs to.
 * @param dBEngine - The database engine that is supposed to execute the function (contains connection to the database).
 * @tparam I - The type covering the input fields of the database function.
 * @tparam R - The type covering the returned fields from the database function.
 * @tparam E - The type of the [[DBEngine]] engine.
 * @tparam F - The type of the context in which the database function is executed.
 */
abstract class DBFunction[I, R, E <: DBEngine[F], F[_]: Monad](functionNameOverride: Option[String] = None)(implicit
  override val schema: DBSchema,
  val dBEngine: E
) extends DBFunctionFabric(functionNameOverride) {

  // A constructor that takes only the mandatory parameters and uses default values for the optional ones
  def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

  // A constructor that allows specifying the function name as a string, but not as an option
  def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

  /**
   * Function to create the DB function call specific to the provided [[DBEngine]].
   * Expected to be implemented by the DBEngine specific mix-in.
   * @param values - The values to pass over to the database function.
   * @return - The SQL query in the format specific to the provided [[DBEngine]].
   */
  protected def query(values: I): dBEngine.QueryType[R]

  /**
   * Executes the database function and returns multiple results.
   * @param values - The values to pass over to the database function.
   * @return - A sequence of results from the database function.
   */
  protected def multipleResults(values: I): F[Seq[R]] = dBEngine.fetchAll(query(values))

  /**
   * Executes the database function and returns a single result.
   * @param values - The values to pass over to the database function.
   * @return - A single result from the database function.
   */
  protected def singleResult(values: I): F[R] = dBEngine.fetchHead(query(values))

  /**
   * Executes the database function and returns an optional result.
   * @param values - The values to pass over to the database function.
   * @return - An optional result from the database function.
   */
  protected def optionalResult(values: I): F[Option[R]] = dBEngine.fetchHeadOption(query(values))
}

/**
 * `DBFunctionWithStatus` is an abstract class that represents a database function with a status.
 * It extends the [[DBFunction]] class and adds handling for the status of the function invocation.
 * @param functionNameOverride - Optional parameter to override the class name if it does not match the database function name.
 * @param schema - The schema the function belongs to.
 * @param dBEngine - The database engine that is supposed to execute the function (contains connection to the database).
 * @param queryStatusHandling - The [[QueryStatusHandling]] instance that handles the status of the function invocation.
 * @tparam I - The type covering the input fields of the database function.
 * @tparam R - The type covering the returned fields from the database function.
 * @tparam E - The type of the [[DBEngine]] engine.
 * @tparam F - The type of the context in which the database function is executed.
 */
abstract class DBFunctionWithStatus[I, R, E <: DBEngine[F], F[_]: Monad](functionNameOverride: Option[String] = None)(
  implicit
  override val schema: DBSchema,
  val dBEngine: E
) extends DBFunctionFabric(functionNameOverride)
    with QueryStatusHandling {

  // A constructor that takes only the mandatory parameters and uses default values for the optional ones
  def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

  // A constructor that allows specifying the function name as a string, but not as an option
  def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

  /**
   *  Function to create the DB function call specific to the provided [[DBEngine]]. Expected to be implemented by the
   *  DBEngine specific mix-in.
   *  @param values the values to pass over to the database function
   *  @return       the SQL query in the format specific to the provided [[DBEngine]]
   */
  protected def query(values: I): dBEngine.QueryWithStatusType[R]

  /**
   * Executes the database function and returns multiple results.
   * @param values
   * @return A sequence of results from the database function.
   */
  def apply(values: I): F[Either[StatusException, R]] = dBEngine.runWithStatus(query(values))

  val defaultStatusField = "status"
  val defaultStatusTextField = "statusText"

  /**
   *  The fields to select from the database function call
   *  @return the fields to select from the database function call
   */
  override def fieldsToSelect: Seq[String] = {
    Seq(
      schema.namingConvention.stringPerConvention(defaultStatusField),
      schema.namingConvention.stringPerConvention(defaultStatusTextField)
    ) ++ super.fieldsToSelect
  }

  // To be provided by an implementation of QueryStatusHandling
  override def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A]
}

object DBFunction {

/**
 * `DBMultipleResultFunction` is an abstract class that represents a database function returning multiple results.
 * It extends the [[DBFunction]] class and overrides the apply method to return a sequence of results.
 */
  abstract class DBMultipleResultFunction[I, R, E <: DBEngine[F], F[_]: Monad](
    functionNameOverride: Option[String] = None
  )(implicit schema: DBSchema, dBEngine: E)
      extends DBFunction[I, R, E, F](functionNameOverride) {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
     *  For easy and convenient execution of the DB function call
     *  @param values - the values to pass over to the database function
     *  @return       - a sequence of values, each coming from a row returned from the DB function transformed to scala
     *               type `R`
     */
    def apply(values: I): F[Seq[R]] = multipleResults(values)
  }

/**
 * `DBSingleResultFunction` is an abstract class that represents a database function returning a single result.
 * It extends the [[DBFunction]] class and overrides the apply method to return a single result.
 */
  abstract class DBSingleResultFunction[I, R, E <: DBEngine[F], F[_]: Monad](
    functionNameOverride: Option[String] = None
  )(implicit schema: DBSchema, dBEngine: E)
      extends DBFunction[I, R, E, F](functionNameOverride) {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
     *  For easy and convenient execution of the DB function call
     *  @param values - the values to pass over to the database function
     *  @return       - the value returned from the DB function transformed to scala type `R`
     */
    def apply(values: I): F[R] = singleResult(values)
  }

/**
 * `DBOptionalResultFunction` is an abstract class that represents a database function returning an optional result.
 * It extends the [[DBFunction]] class and overrides the apply method to return an optional result.
 */
  abstract class DBOptionalResultFunction[I, R, E <: DBEngine[F], F[_]: Monad](
    functionNameOverride: Option[String] = None
  )(implicit schema: DBSchema, dBEngine: E)
      extends DBFunction[I, R, E, F](functionNameOverride) {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
     *  For easy and convenient execution of the DB function call
     *  @param values - the values to pass over to the database function
     *  @return       - the value returned from the DB function transformed to scala type `R` if a row is returned, otherwise `None`
     */
    def apply(values: I): F[Option[R]] = optionalResult(values)
  }
}
