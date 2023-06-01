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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.higherKinds

trait DBEngine {

  // in future implementation the convertor might not be needed (ideally)
  protected def run[R, Q <: Query.Aux[R]](query: Q): Future[Seq[R]]

  def execute[R, Q <: Query.Aux[R]](query: Q): Future[Seq[query.RESULT]] = run[R, Q](query)

  def unique[R, Q <: Query.Aux[R]](query: Q): Future[query.RESULT] = {
    run[R, Q](query).map(_.head)
  }

  def option[R, Q <: Query.Aux[R]](query: Q): Future[Option[query.RESULT]] = {
    run[R, Q](query).map(_.headOption)
  }
}

object DBEngine {
  type Aux[Q, R]
}
