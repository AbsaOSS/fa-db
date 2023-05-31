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

import za.co.absa.fadb.DBEngine.Query

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DBEngine {

  type QueryType[_]

  // in future implementation the convertor might not be needed (ideally)
  protected def run[R](query: QueryType[R]): Future[Seq[R]]

  def execute[R](query: QueryType[R]): Future[Seq[R]] = run(query)

  def unique[QR, R](query: QueryType, converter: QR => R): Future[R] = {
    run(query, converter).map(_.head)
  }

  def option[QR, R](query: QueryType, converter: QR => R): Future[Option[R]] = {
    run(query, converter).map(_.headOption)
  }
}

object DBEngine {
  trait Query
}
