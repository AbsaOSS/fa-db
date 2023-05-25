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

import scala.concurrent.Future

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
abstract class DBFunction[E, T, R](schema: DBSchema[E], functionNameOverride: Option[String] = None) {
  val functionName: String = {
    val fn = functionNameOverride.getOrElse(schema.objectNameFromClassName(getClass))
    if (schema.schemaName.isEmpty) {
      fn
    } else {
      s"${schema.schemaName}.$fn"
    }
  }

  protected def queryFunction(values: T): QueryFunction[E, R]
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
  abstract class DBSeqFunction[E, T, R](schema: DBSchema[E], functionNameOverride: Option[String] = None)
    extends DBFunction[E, T, R](schema, functionNameOverride) {
    def apply(values: T): Future[Seq[R]] = {
      schema.execute(queryFunction(values))
    }
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
  abstract class DBUniqueFunction[E, T, R](schema: DBSchema[E], functionNameOverride: Option[String] = None)
    extends DBFunction[E, T, R](schema, functionNameOverride) {
    def apply(values: T): Future[R] = {
      schema.unique(queryFunction(values))
    }
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
  abstract class DBOptionFunction[E, T, R](schema: DBSchema[E], functionNameOverride: Option[String] = None)
    extends DBFunction[E, T, R](schema, functionNameOverride) {
    def apply(values: T): Future[Option[R]] = {
      schema.option(queryFunction(values))
    }
  }
}
