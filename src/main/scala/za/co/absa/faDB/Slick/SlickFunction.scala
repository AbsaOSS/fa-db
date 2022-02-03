/*
 * Copyright 2022 ABSA Group Limited
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

package za.co.absa.faDB.Slick

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.faDB.DBFunction.QueryFunction
import za.co.absa.faDB.enceladus.DatasetSchema2.ExecutorEngineType

trait SlickFunction {
  def makeQueryFunction[R](sql: SQLActionBuilder)(implicit rconv: GetResult[R]): QueryFunction[Database, R] = {
    val query = sql.as[R]
    val resultFnc = {db: ExecutorEngineType => db.run(query)}
    resultFnc
  }
}
