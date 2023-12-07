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
import za.co.absa.fadb.naming.NamingConvention
import za.co.absa.fadb.status.StatusException

import scala.language.higherKinds

/**
  *
  * @param functionNameOverride - in case the class name would not match the database function name, this gives the
  *                             possibility of override
  * @param schema               - the schema the function belongs into
  * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
  *                             connection to the database
  * @tparam I                   - the type covering the input fields of the database function
  * @tparam R                   - the type covering the returned fields from the database function
  * @tparam E                   - the type of the [[DBEngine]] engine
  */
abstract class DBFunction[I, R, E <: DBEngine[F], F[_]: Monad](functionNameOverride: Option[String] = None)
                                              (implicit override val schema: DBSchema, val dBEngine: E)
  extends DBFunctionFabric(functionNameOverride) {

  // A constructor that takes only the mandatory parameters and uses default values for the optional ones
  def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

  // A constructor that allows specifying the function name as a string, but not as an option
  def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

  /**
    * Function to create the DB function call specific to the provided [[DBEngine]]. Expected to be implemented by the
    * DBEngine specific mix-in.
    * @param values - the values to pass over to the database function
    * @return       - the SQL query in the format specific to the provided [[DBEngine]]
    */
  protected def query(values: I): dBEngine.QueryType[R]

  /*these 3 functions has to be defined here and not in the ancestors, as there the query type is not compatible - path-dependent types*/
  protected def multipleResults(values: I): F[Seq[R]] = dBEngine.fetchAll(query(values))
  protected def singleResult(values: I): F[R] = dBEngine.fetchHead(query(values))
  protected def optionalResult(values: I): F[Option[R]] = dBEngine.fetchHeadOption(query(values))
}

abstract class DBFunctionWithStatusHandling[I, R, E <: DBEngine[F], F[_]: Monad](functionNameOverride: Option[String] = None)
                                                              (implicit override val schema: DBSchema, val dBEngine: E)
  extends DBFunctionFabric(functionNameOverride) {

  // A constructor that takes only the mandatory parameters and uses default values for the optional ones
  def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

  // A constructor that allows specifying the function name as a string, but not as an option
  def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

  /**
   * Function to create the DB function call specific to the provided [[DBEngine]]. Expected to be implemented by the
   * DBEngine specific mix-in.
   * @param values - the values to pass over to the database function
   * @return       - the SQL query in the format specific to the provided [[DBEngine]]
   */
  protected def query(values: I): dBEngine.QueryWithStatusType[R]

  protected def singleResultWithStatusHandling(values: I): F[Either[StatusException, R]] = {
    dBEngine.fetchHeadWithStatusHandling(query(values))
  }
}

object DBFunction {
  /**
    * Represents a function returning a set (in DB sense) of rows
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam I                   - the type covering the input fields of the database function
    * @tparam R                   - the type covering the returned fields from the database function
    * @tparam E                   - the type of the [[DBEngine]] engine
    */
  abstract class DBMultipleResultFunction[I, R, E <: DBEngine[F], F[_]: Monad](functionNameOverride: Option[String] = None)
                                                              (implicit schema: DBSchema, dBEngine: E)
    extends DBFunction[I, R, E, F](functionNameOverride) {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
      * For easy and convenient execution of the DB function call
      * @param values - the values to pass over to the database function
      * @return       - a sequence of values, each coming from a row returned from the DB function transformed to scala
      *               type `R`
      */
    def apply(values: I): F[Seq[R]] = multipleResults(values)
  }

  /**
    * Represents a function returning exactly one record
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam I                   - the type covering the input fields of the database function
    * @tparam R                   - the type covering the returned fields from the database function
    * @tparam E                   - the type of the [[DBEngine]] engine
    */
  abstract class DBSingleResultFunction[I, R, E <: DBEngine[F], F[_]: Monad](functionNameOverride: Option[String] = None)
                                                            (implicit schema: DBSchema, dBEngine: E)
    extends DBFunction[I, R, E, F](functionNameOverride) {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
      * For easy and convenient execution of the DB function call
      * @param values - the values to pass over to the database function
      * @return       - the value returned from the DB function transformed to scala type `R`
      */
    def apply(values: I): F[R] = singleResult(values)
  }

  /**
    * Represents a function returning one optional record
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam I                   - the type covering the input fields of the database function
    * @tparam R                   - the type covering the returned fields from the database function
    * @tparam E                   - the type of the [[DBEngine]] engine
    */
  abstract class DBOptionalResultFunction[I, R, E <: DBEngine[F], F[_]: Monad](functionNameOverride: Option[String] = None)
                                                              (implicit schema: DBSchema, dBEngine: E)
    extends DBFunction[I, R, E, F](functionNameOverride) {

    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

    // A constructor that allows specifying the function name as a string, but not as an option
    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

    /**
      * For easy and convenient execution of the DB function call
      * @param values - the values to pass over to the database function
      * @return       - the value returned from the DB function transformed to scala type `R` if a row is returned, otherwise `None`
      */
    def apply(values: I): F[Option[R]] = optionalResult(values)
  }

//  abstract class DBSingleResultWithStatusHandlingFunction[I, R, E <: DBEngine[F], F[_] : Monad](functionNameOverride: Option[String] = None)
//                                                                             (implicit schema: DBSchema, dBEngine: E)
//    extends DBFunctionWithStatusHandling[I, R, E, F](functionNameOverride) {
//
//    // A constructor that takes only the mandatory parameters and uses default values for the optional ones
//    def this()(implicit schema: DBSchema, dBEngine: E) = this(None)
//
//    // A constructor that allows specifying the function name as a string, but not as an option
//    def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))
//
//    /**
//     * For easy and convenient execution of the DB function call
//     *
//     * @param values - the values to pass over to the database function
//     * @return - the value returned from the DB function transformed to scala type `R`
//     */
//    def apply(values: I): F[Either[StatusException, R]] = singleResultWithStatusHandling(values)
//  }
}
