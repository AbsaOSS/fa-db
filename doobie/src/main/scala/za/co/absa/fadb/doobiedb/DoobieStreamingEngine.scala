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

package za.co.absa.fadb.doobiedb

import cats.effect.Async
import doobie.Transactor
import doobie.implicits.toDoobieStreamOps
import doobie.util.Read
import za.co.absa.fadb.DBStreamingEngine

/**
 *  `DoobieStreamingEngine` is a class that represents a database engine.
 *  It provides methods to execute streaming queries from a database.
 *  @tparam F - The type of the context in which the database queries are executed.
 */
class DoobieStreamingEngine[F[_]: Async](val transactor: Transactor[F], chunkSize: Int = 512)
    extends DBStreamingEngine[F] {

  /** The type of Doobie queries that produce `T` */
  type QueryType[R] = DoobieQuery[R]

  /**
   *  Executes a Doobie query and returns the result as an `fs2.Stream[F, R]`.
   *
   *  @param query the Doobie query to execute
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @return the query result as an `fs2.Stream[F, R]`
   */
  override def runStreaming[R](query: QueryType[R]): fs2.Stream[F, R] =
    executeStreamingQuery(query)(query.readR)

  /**
   *  Executes a Doobie query and returns the result as an `fs2.Stream[F, R]`.
   *
   *  @param query the Doobie query to execute
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @return the query result as an `fs2.Stream[F, R]`
   */
  private def executeStreamingQuery[R](query: QueryType[R])(implicit readR: Read[R]): fs2.Stream[F, R] = {
    query.fragment.query[R].streamWithChunkSize(chunkSize).transact(transactor)
  }

}
