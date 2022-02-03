/*
 * Copyright 2021 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.faDB

import za.co.absa.faDB.DBFunction.QueryFunction

import scala.concurrent.Future

abstract class DBFunction[E, T, R](schema: DBSchema2[E], functionNameOverride: Option[String] = Some("a")) {
  val functionName: String = {
    val fn = functionNameOverride.getOrElse(schema.objectNameFromClassName(getClass))
    if (schema.schemaName.isEmpty) {
      fn
    } else {
      s"${schema.schemaName}.$fn}"
    }
  }

  protected def queryFunction(values: T): QueryFunction[E, R]
}

object DBFunction {
  type QueryFunction[E, R] = (E => Future[Seq[R]])

  abstract class DBSeqFunction[E, T, R](schema: DBSchema2[E], functionNameOverride: Option[String] = None)
    extends DBFunction[E, T, R](schema, functionNameOverride) {
    def apply(values: T): Future[Seq[R]] = {
      schema.execute(queryFunction(values))
    }
  }

  abstract class DBUniqueFunction[E, T, R](schema: DBSchema2[E], functionNameOverride: Option[String] = None)
    extends DBFunction[E, T, R](schema, functionNameOverride) {
    def apply(values: T): Future[R] = {
      schema.unique(queryFunction(values))
    }
  }

  abstract class DBOptionFunction[E, T, R](schema: DBSchema2[E], functionNameOverride: Option[String] = None)
    extends DBFunction[E, T, R](schema, functionNameOverride) {
    def apply(values: T): Future[Option[R]] = {
      schema.option(queryFunction(values))
    }
  }
}
