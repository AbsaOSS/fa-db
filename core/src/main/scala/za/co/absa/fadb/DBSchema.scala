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

import za.co.absa.fadb.naming.NamingConvention

/**
 *  An abstract class, an ancestor to represent a database schema
 *  The database name of the schema is derived from the class name based on the provided naming convention
 *  @param schemaNameOverride - in case the class name would not match the database schema name, this gives the
 *  @param namingConvention   - the [[za.co.absa.fadb.naming.NamingConvention NamingConvention]]
 *                           prescribing how to convert a class name into a db object name
 */
abstract class DBSchema(schemaNameOverride: Option[String] = None)(implicit val namingConvention: NamingConvention) {

  def this(schemaNameOverride: String)(implicit namingConvention: NamingConvention) = {
    this(Option(schemaNameOverride))(namingConvention)
  }

  def this()(implicit namingConvention: NamingConvention) = {
    this(None)(namingConvention)
  }

  def this(namingConvention: NamingConvention, schemaNameOverride: String) = {
    this(Option(schemaNameOverride))(namingConvention)
  }

  /**
   *  To easy pass over to [[DBFunction]] members of the schema
   */
  protected implicit val schema: DBSchema = this

  /**
   *  Function to convert a class to the associated DB object name, based on the class' name. For transformation from the
   *  class name to usual db name the schema's [[za.co.absa.fadb.naming.NamingConvention NamingConvention]] is used.
   *  @param c  - class which name to use to get the DB object name
   *  @return   - the db object name
   */
  def objectNameFromClassName(c: Class[_]): String = {
    namingConvention.fromClassNamePerConvention(c)
  }

  /**
   *  Name of the schema. Based on the schema's class name or provided override
   */
  val schemaName: String = schemaNameOverride.getOrElse(objectNameFromClassName(getClass))

}
