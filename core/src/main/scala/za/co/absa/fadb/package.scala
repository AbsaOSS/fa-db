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

package za.co.absa

import scala.concurrent.Future

package object fadb {
  /**
    * Represents a database query call (in the model of Fa-Db a call to a DB stored procedure). When provided a DB
    * connection (of type [[DBExecutor]]) it executes the query and transforms it to the desired result type sequence.
    * @tparam E - the type of the DB connection to execute on
    * @tparam R - the type of result
    */
  type QueryFunction[E, R] = E => Future[Seq[R]]
}
