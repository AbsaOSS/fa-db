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


import za.co.absa.fadb.DBEngine

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._

import scala.language.higherKinds

/**
  * [[DBEngine]] based on the Slick library in the Postgres flavor
  * @param db - the Slick database
  */
class SlickPgEngine(val db: Database) extends DBEngine {

  /**
    * The type of Queries for Slick
    * @tparam X - the return type of the query
    */
  type QueryType[X] = SlickQuery[X]

  /**
    * Execution using Slick
    * @param query  - the Slick query to execute
    * @tparam R     - return the of the query
    * @return       - sequence of the results of database query
    */
  override protected def run[R](query: QueryType[R]): Future[Seq[R]] = {
    // It can be expected that a GetResult will be passed into the run function as converter.
    // Unfortunately it has to be recreated to be used by Slick
    val slickAction = query.sql.as[R](query.getResult)
    db.run(slickAction)
  }

}
