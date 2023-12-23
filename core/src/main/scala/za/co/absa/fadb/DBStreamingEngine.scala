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

import scala.language.higherKinds

/**
 *  `DBStreamingEngine` is an abstract class that represents a database engine.
 *  It provides methods to execute streaming queries from a database.
 *  @tparam F - The type of the context in which the database queries are executed.
 */
abstract class DBStreamingEngine[F[_]] {

  /**
   *  A type representing the (SQL) query within the engine
   *  @tparam R - the return type of the query
   */
  type QueryType[R] <: Query[R]

  /**
   *  The actual query executioner of the queries of the engine
   *  @param query  - the query to execute
   *  @tparam R     - return type of the query
   *  @return       - stream of the results of database query
   */
  def runStreaming[R](query: QueryType[R]): fs2.Stream[F, R]

}
