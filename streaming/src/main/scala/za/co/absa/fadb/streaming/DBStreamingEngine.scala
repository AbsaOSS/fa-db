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

package za.co.absa.fadb.streaming

import za.co.absa.fadb.Query

import scala.language.higherKinds

/**
 *  [[DBStreamingEngine]] is an abstract class that represents a database engine.
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
   *  Executes a query and returns the result as an [[fs2.Stream]] for effect type `F` and value type `R`.
   *
   *  @param query the query to execute
   *  @return the query result as an [[fs2.Stream]] for effect type `F` and value type `R`
   */
  def runStreaming[R](query: QueryType[R]): fs2.Stream[F, R]

  /**
   *  Executes a query and returns the result as an [[fs2.Stream]] for effect type `F` and value type `R`.
   *
   *  @param query the query to execute
   *  @param chunkSize the chunk size to use when streaming the query result
   *  @return the query result as an [[fs2.Stream]] for effect type `F` and value type `R`
   */
  def runStreamingWithChunkSize[R](query: QueryType[R], chunkSize: Int): fs2.Stream[F, R]

}
