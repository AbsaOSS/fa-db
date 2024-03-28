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

import cats.implicits._
import slick.jdbc.PostgresProfile.api._
import za.co.absa.fadb.DBEngine
import za.co.absa.fadb.exceptions.StatusException

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

/**
 *  [[DBEngine]] based on the Slick library in the Postgres flavor
 *
 *  @param db - the Slick database
 */
class SlickPgEngine(val db: Database)(implicit val executor: ExecutionContext) extends DBEngine[Future] {

  /**
   *  The type of Queries for Slick
   *  @tparam R - the return type of the query
   */
  type QueryType[R] = SlickQuery[R]
  type QueryWithStatusType[R] = SlickQueryWithStatus[R]

  /**
   *  Execution using Slick
   *  @param query  - the Slick query to execute
   *  @tparam R     - return the of the query
   *  @return       - sequence of the results of database query
   */
  override protected def run[R](query: QueryType[R]): Future[Seq[R]] = {
    val slickAction = query.sql.as[R](query.getResult)
    db.run(slickAction)
  }

  /**
   *  Execution using Slick with status
   *  @param query  - the Slick query to execute
   *  @tparam R     - return the of the query
   *  @return       - either status exception or result of database query
   */
  override def runWithStatus[R](query: QueryWithStatusType[R]): Future[Seq[DBEngine.ExceptionOrStatusWithData[R]]] = {
    val slickAction = query.sql.as[DBEngine.ExceptionOrStatusWithData[R]](query.getStatusExceptionOrData)
    db.run(slickAction)
  }
}
