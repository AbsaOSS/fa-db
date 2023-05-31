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

package za.co.absa.fadb.slick

import slick.jdbc.{GetResult, SQLActionBuilder}
import slick.jdbc.PostgresProfile.api._
import za.co.absa.fadb.{DBFunctionFabric, QueryFunction}

trait SlickPgFunction[T, R] extends DBFunctionFabric {

  protected def sqlToCallFunction(values: T): SQLActionBuilder

  protected def resultConverter: GetResult[R]

  protected def makeQueryFunction(sql: SQLActionBuilder)(implicit rconv: GetResult[R]): QueryFunction[Database, R] = {
    val query = sql.as[R]
    val resultFnc = {db: Database => db.run(query)}
    resultFnc
  }

  protected def queryFunction(values: T): QueryFunction[Database, R] = {
    val converter = resultConverter
    val functionSql = sqlToCallFunction(values)
    makeQueryFunction(functionSql)(converter)
  }
}
