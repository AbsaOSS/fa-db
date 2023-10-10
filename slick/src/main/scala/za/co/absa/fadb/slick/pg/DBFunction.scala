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

package za.co.absa.fadb.slick.pg

import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.{SlickFunction, SlickFunctionWithStatusSupport, SlickPgEngine}
import za.co.absa.fadb.DBFunction.{
  DBMultipleResultFunction => CoreDBMultipleResultFunction,
  DBOptionalResultFunction => CoreDBOptionalResultFunction,
  DBSingleResultFunction => CoreDBSingleResultFunction
}

object DBFunction {
  /**
    * Combines [[za.co.absa.fadb.DBFunction.DBMultipleResultFunction DBMultipleResultFunction]] with Slick and Postgres support
    * @see See [[za.co.absa.fadb.DBFunction.DBMultipleResultFunction]] and [[za.co.absa.fadb.slick.SlickFunction]] for methods description
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
    * Combines [[za.co.absa.fadb.DBFunction.DBSingleResultFunction DBSingleResultFunction]] with Slick and Postgres support
    * @see See [[za.co.absa.fadb.DBFunction.DBSingleResultFunction]] and [[za.co.absa.fadb.slick.SlickFunction]] for methods description
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
    * Combines [[za.co.absa.fadb.DBFunction.DBOptionalResultFunction DBOptionalResultFunction]] with Slick and Postgres support
    * @see See [[za.co.absa.fadb.DBFunction.DBOptionalResultFunction]] and [[za.co.absa.fadb.slick.SlickFunction]] for methods description
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
    * Combines [[za.co.absa.fadb.DBFunction.DBMultipleResultFunction DBMultipleResultFunction]] with Slick, Postgres and
    * status codes support
    * @see See [[za.co.absa.fadb.DBFunction.DBMultipleResultFunction]] and [[za.co.absa.fadb.slick.SlickFunctionWithStatusSupport]] for methods description
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
    * Combines [[za.co.absa.fadb.DBFunction.DBSingleResultFunction DBSingleResultFunction]] with Slick, Postgres and
    * status codes support
    * @see See [[za.co.absa.fadb.DBFunction.DBSingleResultFunction]] and [[za.co.absa.fadb.slick.SlickFunctionWithStatusSupport]] for methods description
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
    * Combines [[za.co.absa.fadb.DBFunction.DBOptionalResultFunction DBOptionalResultFunction]] with Slick, Postgres and
    * status codes support
    * @see See [[za.co.absa.fadb.DBFunction.DBOptionalResultFunction]] and [[za.co.absa.fadb.slick.SlickFunctionWithStatusSupport]] for methods description
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
