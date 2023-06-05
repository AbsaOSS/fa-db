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

import za.co.absa.fadb.naming_conventions.NamingConvention

import scala.concurrent.Future

/**
  *
  * @param functionNameOverride - in case the class name would not match the database function name, this gives the
  *                             possibility of override
  * @param schema               - the schema the function belongs into
  * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
  *                             connection to the database
  * @tparam T                   - the type covering the input fields of the database function
  * @tparam R                   - the type covering the returned fields from the database function
  * @tparam E                   - the type of the [[DBEngine]] engine
  */
abstract class DBFunction[T, R, E <: DBEngine](functionNameOverride: Option[String] = None)
                                              (implicit val schema: DBSchema, val dBEngine: E)  extends DBFunctionFabric {

  /* alternative constructors for different availability of input parameters */
  def this(schema: DBSchema, functionNameOverride: String)
         (implicit dBEngine: E) = {
    this(Option(functionNameOverride))(schema, dBEngine)
  }

  /* only one constructor of a class can have default values for parameters*/
  def this(schema: DBSchema)
          (implicit dBEngine: E) = {
    this(None)(schema, dBEngine)
  }

  def this(dBEngine: E, functionNameOverride: String)
          (implicit schema: DBSchema)  = {
    this(Option(functionNameOverride))(schema, dBEngine)
  }

  def this(dBEngine: E)
          (implicit schema: DBSchema)  = {
    this(None)(schema, dBEngine)
  }

  /**
    * Function to create the DB function call specific to the provided [[DBEngine]]. Expected to be implemented by the
    * DBEngine specific mix-in.
    * @param values - the values to pass over to the database function
    * @return       - the SQL query in the format specific to the provided [[DBEngine]]
    */
  protected def query(values: T): dBEngine.QueryType[R]

  /**
    * Name of the function, based on the class name, unless it is overridden in the constructor
    */
  val functionName: String = {
    val fn = functionNameOverride.getOrElse(schema.objectNameFromClassName(getClass))
    if (schema.schemaName.isEmpty) {
      fn
    } else {
      s"${schema.schemaName}.$fn"
    }
  }

  def namingConvention: NamingConvention = schema.namingConvention

  /**
    * List of fields to select from the DB function. Expected to be based on the return type `R`
    * @return - list of fields to select
    */
  override protected def fieldsToSelect: Seq[String] = super.fieldsToSelect //TODO should get the names from R #6

  /*these 3 functions has to be defined here and not in the ancestors, as there the query type is not compatible - path-dependent types*/
  protected def execute(values: T): Future[Seq[R]] = dBEngine.execute[R](query(values))
  protected def unique(values: T): Future[R] = dBEngine.unique(query(values))
  protected def option(values: T): Future[Option[R]] = dBEngine.option(query(values))

}

object DBFunction {
  /**
    * Represents a function returning a set (in DB sense) of rows
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam T                   - the type covering the input fields of the database function
    * @tparam R                   - the type covering the returned fields from the database function
    * @tparam E                   - the type of the [[DBEngine]] engine
    */
  abstract class DBSeqFunction[T, R, E <: DBEngine](functionNameOverride: Option[String] = None)(implicit schema: DBSchema, dBEngine: E)
    extends DBFunction[T, R, E](functionNameOverride) {

    def this(schema: DBSchema, functionNameOverride: String)
            (implicit dBEngine: E) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema)
            (implicit dBEngine: E) = {
      this(None)(schema, dBEngine)
    }

    def this(dBEngine: E, functionNameOverride: String)
            (implicit schema: DBSchema)  = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(dBEngine: E)
            (implicit schema: DBSchema)  = {
      this(None)(schema, dBEngine)
    }

    /**
      * For easy and convenient execution of the DB function call
      * @param values - the values to pass over to the database function
      * @return       - a sequence of values, each coming from a row returned from the DB function transformed to scala
      *               type `R`
      */
    def apply(values: T): Future[Seq[R]] = execute(values)
  }

  /**
    * Represents a function returning exactly one record
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam T                   - the type covering the input fields of the database function
    * @tparam R                   - the type covering the returned fields from the database function
    * @tparam E                   - the type of the [[DBEngine]] engine
    */
  abstract class DBUniqueFunction[T, R, E <: DBEngine](functionNameOverride: Option[String] = None)(implicit schema: DBSchema, dBEngine: E)
    extends DBFunction[T, R, E](functionNameOverride) {

    def this(schema: DBSchema, functionNameOverride: String)
            (implicit dBEngine: E) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema)
            (implicit dBEngine: E) = {
      this(None)(schema, dBEngine)
    }

    def this(dBEngine: E, functionNameOverride: String)
            (implicit schema: DBSchema)  = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(dBEngine: E)
            (implicit schema: DBSchema)  = {
      this(None)(schema, dBEngine)
    }

    /**
      * For easy and convenient execution of the DB function call
      * @param values - the values to pass over to the database function
      * @return       - the value returned from the DB function transformed to scala type `R`
      */
    def apply(values: T): Future[R] = unique(values)
  }

  /**
    * Represents a function returning one optional record
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam T                   - the type covering the input fields of the database function
    * @tparam R                   - the type covering the returned fields from the database function
    * @tparam E                   - the type of the [[DBEngine]] engine
    */
  abstract class DBOptionFunction[T, R, E <: DBEngine](functionNameOverride: Option[String] = None)(implicit schema: DBSchema, dBEngine: E)
    extends DBFunction[T, R, E](functionNameOverride) {

    def this(schema: DBSchema, functionNameOverride: String)
            (implicit dBEngine: E) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema)
            (implicit dBEngine: E) = {
      this(None)(schema, dBEngine)
    }

    def this(dBEngine: E, functionNameOverride: String)
            (implicit schema: DBSchema)  = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(dBEngine: E)
            (implicit schema: DBSchema)  = {
      this(None)(schema, dBEngine)
    }

    /**
      * For easy and convenient execution of the DB function call
      * @param values - the values to pass over to the database function
      * @return       - the value returned from the DB function transformed to scala type `R` if a row is returned, otherwise `None`
      */
    def apply(values: T): Future[Option[R]] = option(values)
  }
}
