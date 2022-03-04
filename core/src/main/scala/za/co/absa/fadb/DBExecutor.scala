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

import za.co.absa.fadb.DBFunction.QueryFunction

import scala.concurrent.Future

/**
  * And abstraction to make it possible to execute queries through regardless of the provided database engine library
  *
  * @tparam E - the type of the engine, E.g. a Slick Postgres Database
  */
trait DBExecutor[E] {
  def run[R](fnc: QueryFunction[E, R]): Future[Seq[R]]
}
