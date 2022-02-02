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

import scala.concurrent.Future

abstract class DBFunction[Q](schema: DBSchema2[Q], functionNameOverride: Option[String] = Some("a")) {
  val functionName: String = {
    val fn = functionNameOverride.getOrElse(schema.objectNameFromClassName(getClass))
    if (schema.schemaName.isEmpty) {
      fn
    } else {
      s"${schema.schemaName}.$fn}"
    }
  }

  protected def buildQuery[T](values: T): Q
}

object DBFunction {
  abstract class DBSeqFunction[T, R](schema: DBSchema, functionNameOverride: Option[String] = None)
    extends DBFunction(schema, functionNameOverride) {
    def apply(values: T): Future[Seq[R]] = {
      schema.execute(buildQuery(values))
    }
  }

  abstract class DBUniqueFunction[Q, T, R](schema: DBSchema2[Q], functionNameOverride: Option[String] = None)
    extends DBFunction(schema, functionNameOverride) {
    def apply(values: T): Future[R] = {
      schema.unique(buildQuery(values))
    }
  }

  abstract class DBOptionFunction[Q, T, R](schema: DBSchema2[Q], functionNameOverride: Option[String] = None)
    extends DBFunction (schema, functionNameOverride) {
    def apply(values: T): Future[Option[R]] = {
      schema.option(buildQuery(values))
    }
  }
}
