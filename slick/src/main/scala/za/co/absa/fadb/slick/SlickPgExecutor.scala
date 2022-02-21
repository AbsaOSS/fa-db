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


import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._

import za.co.absa.fadb.DBFunction.QueryFunction
import za.co.absa.fadb.DBExecutor


class SlickPgExecutor(db: Database) extends DBExecutor[Database] {
  override def run[R](fnc: QueryFunction[Database, R]): Future[Seq[R]] = {
    fnc(db)
  }
}

object SlickPgExecutor {
  def forConfig(dbConfig: String): SlickPgExecutor = {
    val db = Database.forConfig(dbConfig)
    new SlickPgExecutor(db)
  }
}
