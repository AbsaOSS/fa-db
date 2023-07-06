/*
 * Copyright 2023 ABSA Group Limited
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

package za.co.absa.fadb.slick.pg

import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.{SlickFunction, SlickFunctionWithStatusSupport, SlickPgEngine}
import za.co.absa.fadb.DBFunction.{DBMultipleResultFunction => CoreDBMultipleResultFunction, DBOptionalResultFunction => CoreDBOptionalResultFunction, DBSingleResultFunction => CoreDBSingleResultFunction}

object DBFunction {
  /**
    * Represents a function returning a set (in DB sense) of rows
    *
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam I - the type covering the input fields of the database function
    * @tparam R - the type covering the returned fields from the database function
    */
  abstract class DBMultipleResultFunction[I, R](functionNameOverride: Option[String] = None)
                                               (implicit schema: DBSchema, dBEngine: SlickPgEngine)
    extends CoreDBMultipleResultFunction[I, R, SlickPgEngine](functionNameOverride)
      with SlickFunction[I, R] {

    def this(functionNameOverride: String)
            (implicit schema: DBSchema, dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema, functionNameOverride: String)
            (implicit dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema)
            (implicit dBEngine: SlickPgEngine) = {
      this(None)(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine, functionNameOverride: String)
            (implicit schema: DBSchema) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine)
            (implicit schema: DBSchema) = {
      this(None)(schema, dBEngine)
    }
  }

  /**
    * Represents a function returning exactly one record
    *
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam I - the type covering the input fields of the database function
    * @tparam R - the type covering the returned fields from the database function
    */
  abstract class DBSingleResultFunction[I, R](functionNameOverride: Option[String] = None)
                                                            (implicit schema: DBSchema, dBEngine: SlickPgEngine)
    extends CoreDBSingleResultFunction[I, R, SlickPgEngine](functionNameOverride)
    with SlickFunction[I, R] {

    def this(functionNameOverride: String)
            (implicit schema: DBSchema, dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema, functionNameOverride: String)
            (implicit dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema)
            (implicit dBEngine: SlickPgEngine) = {
      this(None)(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine, functionNameOverride: String)
            (implicit schema: DBSchema) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine)
            (implicit schema: DBSchema) = {
      this(None)(schema, dBEngine)
    }
  }

  /**
    * Represents a function returning one optional record
    *
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam I - the type covering the input fields of the database function
    * @tparam R - the type covering the returned fields from the database function
    */
  abstract class DBOptionalResultFunction[I, R](functionNameOverride: Option[String] = None)
                                                              (implicit schema: DBSchema, dBEngine: SlickPgEngine)
    extends CoreDBOptionalResultFunction[I, R, SlickPgEngine](functionNameOverride)
    with SlickFunction[I, R]{

    def this(functionNameOverride: String)
            (implicit schema: DBSchema, dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema, functionNameOverride: String)
            (implicit dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema)
            (implicit dBEngine: SlickPgEngine) = {
      this(None)(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine, functionNameOverride: String)
            (implicit schema: DBSchema) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine)
            (implicit schema: DBSchema) = {
      this(None)(schema, dBEngine)
    }
  }

// ---------------------------------------------------------------------------------------------------------------------

  /**
    * Represents a function returning a set (in DB sense) of rows
    *
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam I - the type covering the input fields of the database function
    * @tparam R - the type covering the returned fields from the database function
    */
  abstract class DBMultipleResultFunctionWithStatusSupport[I, R](functionNameOverride: Option[String] = None)
                                                                (implicit schema: DBSchema, dBEngine: SlickPgEngine)
    extends CoreDBMultipleResultFunction[I, R, SlickPgEngine](functionNameOverride)
    with SlickFunctionWithStatusSupport[I, R] {

    def this(functionNameOverride: String)
            (implicit schema: DBSchema, dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema, functionNameOverride: String)
            (implicit dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema)
            (implicit dBEngine: SlickPgEngine) = {
      this(None)(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine, functionNameOverride: String)
            (implicit schema: DBSchema) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine)
            (implicit schema: DBSchema) = {
      this(None)(schema, dBEngine)
    }
  }

  /**
    * Represents a function returning exactly one record
    *
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam I - the type covering the input fields of the database function
    * @tparam R - the type covering the returned fields from the database function
    */
  abstract class DBSingleResultFunctionWithStatusSupport[I, R](functionNameOverride: Option[String] = None)
                                                              (implicit schema: DBSchema, dBEngine: SlickPgEngine)
    extends CoreDBSingleResultFunction[I, R, SlickPgEngine](functionNameOverride)
    with SlickFunctionWithStatusSupport[I, R] {

    def this(functionNameOverride: String)
            (implicit schema: DBSchema, dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema, functionNameOverride: String)
            (implicit dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema)
            (implicit dBEngine: SlickPgEngine) = {
      this(None)(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine, functionNameOverride: String)
            (implicit schema: DBSchema) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine)
            (implicit schema: DBSchema) = {
      this(None)(schema, dBEngine)
    }
  }

  /**
    * Represents a function returning one optional record
    *
    * @param functionNameOverride - in case the class name would not match the database function name, this gives the
    *                             possibility of override
    * @param schema               - the schema the function belongs into
    * @param dBEngine             - the database engine that is supposed to execute the function (presumably contains
    *                             connection to the database
    * @tparam I - the type covering the input fields of the database function
    * @tparam R - the type covering the returned fields from the database function
    */
  abstract class DBOptionalResultFunctionWithStatusSupport[I, R](functionNameOverride: Option[String] = None)
                                                                (implicit schema: DBSchema, dBEngine: SlickPgEngine)
    extends CoreDBOptionalResultFunction[I, R, SlickPgEngine](functionNameOverride)
    with SlickFunctionWithStatusSupport[I, R]{

    def this(functionNameOverride: String)
            (implicit schema: DBSchema, dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema, functionNameOverride: String)
            (implicit dBEngine: SlickPgEngine) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(schema: DBSchema)
            (implicit dBEngine: SlickPgEngine) = {
      this(None)(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine, functionNameOverride: String)
            (implicit schema: DBSchema) = {
      this(Option(functionNameOverride))(schema, dBEngine)
    }

    def this(dBEngine: SlickPgEngine)
            (implicit schema: DBSchema) = {
      this(None)(schema, dBEngine)
    }
  }

}
