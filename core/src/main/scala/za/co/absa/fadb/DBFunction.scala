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
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * The most general abstraction of database function representation
  * The database name of the function is derives from the class name based on the provided naming convention (in schema)
  *
  * @param schema               - the schema the function belongs into
  * @param functionNameOverride - in case the class name would not match the database function name, this gives the
  *                             possibility of override
  * @tparam E                   - the type of the [[DBExecutor]] engine
  * @tparam T                   - the type covering the input fields of the database function
  * @tparam R                   - the type covering the returned fields from the database function
  */
abstract class DBFunction[T, R](val schema: DBSchema, functionNameOverride: Option[String] = None)  extends DBFunctionFabric {

  val functionName: String = {
    val fn = functionNameOverride.getOrElse(schema.objectNameFromClassName(getClass))
    if (schema.schemaName.isEmpty) {
      fn
    } else {
      s"${schema.schemaName}.$fn"
    }
  }

  def namingConvention: NamingConvention = schema.namingConvention

  override protected def fieldsToSelect: Seq[String] = super.fieldsToSelect //TODO should get the names from R #6

  protected def query[Q <: schema.dBEngine.QueryType[R]](values: T): Q

  /**
    * For the given output it returns a function to execute the SQL query and interpret the results.
    * Basically it should create a function which contains a query to be executable and executed on on the [[DBExecutor]]
    * and transforming the result of that query to result type.
    * @param values - the input values of the DB function (stored procedure)
    * @return       - the query function that when provided an executor will return the result of the DB function call
    */

  protected def execute(values: T): Future[Seq[R]] = {
    schema.dBEngine.execute(query(values))
  }

  protected def unique(values: T): Future[R] = {
    schema.dBEngine.unique(query(values))
  }

  protected def option(values: T): Future[Option[R]] = {
    schema.dBEngine.option(query(values))
  }

}

object DBFunction {
  /**
    * Represents a function returning a set (in DB sense) of rows
    *
    * @param schema               - the schema the function belongs into
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @tparam E                   - the type of the [[DBExecutor]] engine
    * @tparam T                   - the type covering the input fields of the database function
    * @tparam R                   - the type covering the returned fields from the database function
    */
  abstract class DBSeqFunction[T, R](schema: DBSchema, functionNameOverride: Option[String] = None)
    extends DBFunction[T, R](schema, functionNameOverride) {
    def apply(values: T): Future[Seq[R]] = execute(values)
  }

  /**
    * Represents a function returning exactly one record
    *
    * @param schema               - the schema the function belongs into
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @tparam E                   - the type of the [[DBExecutor]] engine
    * @tparam T                   - the type covering the input fields of the database function
    * @tparam R                   - the type covering the returned fields from the database function
    */
  abstract class DBUniqueFunction[T, R](schema: DBSchema, functionNameOverride: Option[String] = None)
    extends DBFunction[T, R](schema, functionNameOverride) {
    def apply(values: T): Future[R] = unique(values)
  }

  /**
    * Represents a function returning one optional record
    *
    * @param schema               - the schema the function belongs into
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @tparam E                   - the type of the [[DBExecutor]] engine
    * @tparam T                   - the type covering the input fields of the database function
    * @tparam R                   - the type covering the returned fields from the database function
    */
  abstract class DBOptionFunction[T, R](schema: DBSchema, functionNameOverride: Option[String] = None)
    extends DBFunction[T, R](schema, functionNameOverride) {
    def apply(values: T): Future[Option[R]] = option(values)
  }
}
