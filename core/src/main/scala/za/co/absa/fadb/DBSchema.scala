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

/**
  * An abstract class, an ancestor to represent a database schema (each database function should be placed in a schema)
  * The database name of the schema is derives from the class name based on the provided naming convention
  *
  * @param executor           - executor to execute the queries through
  * @param schemaNameOverride - in case the class name would not match the database schema name, this gives the
  *                           possibility of override
  * @param namingConvention   - the [[za.co.absa.fadb.naming_conventions.NamingConvention]](NamingConvention) prescribing how to convert a class name into a db object name
  * @tparam E                 - the engine of the executor type, e.g. Slick Database
  */
abstract class DBSchema(schemaNameOverride: Option[String] = None)
                       (implicit dBEngine: DBEngine, implicit val namingConvention: NamingConvention) {

  def this(dBEngine: DBEngine, schemaNameOverride: String)
          (implicit namingConvention: NamingConvention) {
    this(Option(schemaNameOverride))(dBEngine, namingConvention)
  }


  def this(dBEngine: DBEngine)
          (implicit namingConvention: NamingConvention) {
    this(None)(dBEngine, namingConvention)
  }

  def this(namingConvention: NamingConvention, schemaNameOverride:String)
          (implicit dBEngine: DBEngine) {
    this(Option(schemaNameOverride))(dBEngine, namingConvention)
  }

  def this(namingConvention: NamingConvention)
          (implicit dBEngine: DBEngine) {
    this(None)(dBEngine, namingConvention)
  }

  //type QueryType[R] = dBEngine.QueryType[R]
  protected implicit val schema: DBSchema = this

  def objectNameFromClassName(c: Class[_]): String = {
    namingConvention.fromClassNamePerConvention(c)
  }

  val schemaName: String = schemaNameOverride.getOrElse(objectNameFromClassName(getClass))

}
