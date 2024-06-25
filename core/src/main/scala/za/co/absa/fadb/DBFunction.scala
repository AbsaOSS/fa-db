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

import cats.MonadError
import cats.implicits.toFlatMapOps
import za.co.absa.fadb.status.aggregation.StatusAggregator
import za.co.absa.fadb.status.handling.StatusHandling
import za.co.absa.fadb.status.{FailedOrRows, FailedOrRow, Row}

import scala.language.higherKinds

/**
 *  `DBFunction` is an abstract class that represents a database function.
 *  @param functionNameOverride - Optional parameter to override the class name if it does not match the database function name.
 *  @param schema - The schema the function belongs to.
 *  @param dBEngine - The database engine that is supposed to execute the function (contains connection to the database).
 *  @tparam I - The type covering the input fields of the database function.
 *  @tparam R - The type covering the returned fields from the database function.
 *  @tparam E - The type of the [[DBEngine]] engine.
 *  @tparam F - The type of the context in which the database function is executed.
 */
abstract class DBFunction[I, R, E <: DBEngine[F], F[_]](functionNameOverride: Option[String] = None)(implicit
  override val schema: DBSchema,
  val dBEngine: E
) extends DBFunctionFabric(functionNameOverride) {

  // A constructor that takes only the mandatory parameters and uses default values for the optional ones
  def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

  // A constructor that allows specifying the function name as a string, but not as an option
  def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

  /**
   *  Executes the database function and returns multiple results.
   *  @param values - The values to pass over to the database function.
   *  @return - A sequence of results from the database function.
   */
  protected def multipleResults(values: I)(implicit me: MonadError[F, Throwable]): F[Seq[R]] =
    query(values).flatMap(q => dBEngine.fetchAll(q))

  /**
   *  Executes the database function and returns a single result.
   *  @param values - The values to pass over to the database function.
   *  @return - A single result from the database function.
   */
  protected def singleResult(values: I)(implicit me: MonadError[F, Throwable]): F[R] =
    query(values).flatMap(q => dBEngine.fetchHead(q))

  /**
   *  Executes the database function and returns an optional result.
   *  @param values - The values to pass over to the database function.
   *  @return - An optional result from the database function.
   */
  protected def optionalResult(values: I)(implicit me: MonadError[F, Throwable]): F[Option[R]] = {
    query(values).flatMap(q => dBEngine.fetchHeadOption(q))
  }

  /**
   *  Function to create the DB function call specific to the provided [[DBEngine]].
   *  Expected to be implemented by the DBEngine specific mix-in.
   *  @param values - The values to pass over to the database function.
   *  @return - The SQL query in the format specific to the provided [[DBEngine]].
   */
  protected def query(values: I)(implicit me: MonadError[F, Throwable]): F[dBEngine.QueryType[R]]
}

/**
 *  `DBFunctionWithStatus` is an abstract class that represents a database function with a status.
 *  It extends the [[DBFunction]] class and adds handling for the status of the function invocation.
 *  @param functionNameOverride - Optional parameter to override the class name if it does not match the database function name.
 *  @param schema - The schema the function belongs to.
 *  @param dBEngine - The database engine that is supposed to execute the function (contains connection to the database).
 *  @tparam I - The type covering the input fields of the database function.
 *  @tparam R - The type covering the returned fields from the database function.
 *  @tparam E - The type of the [[DBEngine]] engine.
 *  @tparam F - The type of the context in which the database function is executed.
 */
abstract class DBFunctionWithStatus[I, R, E <: DBEngine[F], F[_]](functionNameOverride: Option[String] = None)(implicit
  override val schema: DBSchema,
  val dBEngine: E
) extends DBFunctionFabric(functionNameOverride)
    with StatusHandling {

  private val defaultStatusField = "status"
  private val defaultStatusTextField = "statusText"

  // A constructor that takes only the mandatory parameters and uses default values for the optional ones
  def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

  // A constructor that allows specifying the function name as a string, but not as an option
  def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

  /**
    *  Executes the database function and returns multiple results.
    *  @param values - The values to pass over to the database function.
    *  @return - A sequence of results from the database function.
    */
  protected def multipleResults(values: I)(implicit me: MonadError[F, Throwable]): F[Seq[FailedOrRow[R]]] =
    query(values).flatMap(q => dBEngine.fetchAllWithStatus(q))

  /**
    *  Executes the database function and returns a single result.
    *  @param values - The values to pass over to the database function.
    *  @return - A single result from the database function.
    */
  protected def singleResult(values: I)(implicit me: MonadError[F, Throwable]): F[FailedOrRow[R]] =
    query(values).flatMap(q => dBEngine.fetchHeadWithStatus(q))

  /**
    *  Executes the database function and returns an optional result.
    *  @param values - The values to pass over to the database function.
    *  @return - An optional result from the database function.
    */
  protected def optionalResult(values: I)(implicit me: MonadError[F, Throwable]): F[Option[FailedOrRow[R]]] =
    query(values).flatMap(q => dBEngine.fetchHeadOptionWithStatus(q))

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

  /**
   *  Function to create the DB function call specific to the provided [[DBEngine]].
   *  @param values the values to pass over to the database function
   *  @return       the SQL query in the format specific to the provided [[DBEngine]]
   */
  protected def query(values: I)(implicit me: MonadError[F, Throwable]): F[dBEngine.QueryWithStatusType[R]]

  // To be provided by an implementation of QueryStatusHandling
  override def checkStatus[D](statusWithData: Row[D]): FailedOrRow[D]
}

object DBFunction {

  /**
   *  `DBMultipleResultFunction` is an abstract class that represents a database function returning multiple results.
   *  It extends the [[DBFunction]] class and overrides the apply method to return a sequence of results.
   */
  abstract class DBMultipleResultFunction[I, R, E <: DBEngine[F], F[_]](
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
    def apply(values: I)(implicit me: MonadError[F, Throwable]): F[Seq[R]] = multipleResults(values)
  }

  /**
   *  `DBSingleResultFunction` is an abstract class that represents a database function returning a single result.
   *  It extends the [[DBFunction]] class and overrides the apply method to return a single result.
   */
  abstract class DBSingleResultFunction[I, R, E <: DBEngine[F], F[_]](
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
    def apply(values: I)(implicit me: MonadError[F, Throwable]): F[R] = singleResult(values)
  }

  /**
   *  `DBOptionalResultFunction` is an abstract class that represents a database function returning an optional result.
   *  It extends the [[DBFunction]] class and overrides the apply method to return an optional result.
   */
  abstract class DBOptionalResultFunction[I, R, E <: DBEngine[F], F[_]](
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
    def apply(values: I)(implicit me: MonadError[F, Throwable]): F[Option[R]] = optionalResult(values)
  }

  /**
    *  `DBMultipleResultFunctionWithStatus` is an abstract class that represents a database function returning
    *  multiple results with status information.
    *  It extends the [[DBFunctionWithStatus]] class and overrides the apply method to return a sequence of results.
    */
  abstract class DBMultipleResultFunctionWithStatus[I, R, E <: DBEngine[F], F[_]](
    functionNameOverride: Option[String] = None
  )(implicit schema: DBSchema, dBEngine: E)
    extends DBFunctionWithStatus[I, R, E, F](functionNameOverride) {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
      *  For easy and convenient execution of the DB function call
      *  @param values - the values to pass over to the database function
      *  @return       - a sequence of values, each coming from a row returned from the DB function transformed to scala
      *               type `R` wrapped around with Either, providing StatusException if raised
      */
    def apply(values: I)(implicit me: MonadError[F, Throwable]): F[Seq[FailedOrRow[R]]] = multipleResults(values)
  }

  /**
   *  `DBMultipleResultFunctionWithAggStatus` is an abstract class that represents a database function returning
   *  multiple results with status information.
   *  It extends the [[DBFunctionWithStatus]] class and overrides the apply method to return a sequence of results
   *
   *  It's similar to `DBMultipleResultFunctionWithStatus` but the statuses are aggregated into a single value.
   *  The algorithm for performing the aggregation is based on provided implementation of `StatusAggregator.aggregate`.
   */
  abstract class DBMultipleResultFunctionWithAggStatus[I, R, E <: DBEngine[F], F[_]](
   functionNameOverride: Option[String] = None
  )(implicit schema: DBSchema, dBEngine: E)
    extends DBFunctionWithStatus[I, R, E, F](functionNameOverride)
      with StatusAggregator {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
     *  For easy and convenient execution of the DB function call
     *  @param values - the values to pass over to the database function
     *  @return       - a sequence of values, each coming from a row returned from the DB function transformed to scala
     *               type `R` wrapped around with Either, providing StatusException if raised
     */
    def apply(values: I)
             (implicit me: MonadError[F, Throwable]): F[FailedOrRows[R]] =
      multipleResults(values).flatMap(data => me.pure(aggregate(data)))
  }

  /**
    *  `DBSingleResultFunctionWithStatus` is an abstract class that represents a database function returning
    *  a single result with status information.
    *  It extends the [[DBFunctionWithStatus]] class and overrides the apply method to return a single result.
    */
  abstract class DBSingleResultFunctionWithStatus[I, R, E <: DBEngine[F], F[_]](
    functionNameOverride: Option[String] = None
  )(implicit schema: DBSchema, dBEngine: E)
    extends DBFunctionWithStatus[I, R, E, F](functionNameOverride) {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
      *  For easy and convenient execution of the DB function call
      *  @param values - the values to pass over to the database function
      *  @return       - the value returned from the DB function transformed to scala type `R`
      *                  wrapped around with Either, providing StatusException if raised
      */
    def apply(values: I)(implicit me: MonadError[F, Throwable]): F[FailedOrRow[R]] =
      singleResult(values)
  }

  /**
    *  `DBOptionalResultFunctionWithStatus` is an abstract class that represents a database function returning
    *  an optional result with status information.
    *  It extends the [[DBFunctionWithStatus]] class and overrides the apply method to return an optional result.
    */
  abstract class DBOptionalResultFunctionWithStatus[I, R, E <: DBEngine[F], F[_]](
    functionNameOverride: Option[String] = None
  )(implicit schema: DBSchema, dBEngine: E)
    extends DBFunctionWithStatus[I, R, E, F](functionNameOverride) {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
      *  For easy and convenient execution of the DB function call
      *  @param values - the values to pass over to the database function
      *  @return       - the value returned from the DB function transformed to scala type `R` if a row is returned,
      *                  otherwise `None`, wrapped around with Either, providing StatusException if raised
      */
    def apply(values: I)(implicit me: MonadError[F, Throwable]): F[Option[FailedOrRow[R]]] =
      optionalResult(values)
  }

}
