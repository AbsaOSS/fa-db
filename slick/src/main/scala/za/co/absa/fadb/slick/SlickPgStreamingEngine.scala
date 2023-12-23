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

import cats.effect.Async
import fs2.interop.reactivestreams.PublisherOps
import slick.jdbc.JdbcBackend.Database
import za.co.absa.fadb.DBStreamingEngine

import scala.language.higherKinds

/**
 *  `SlickStreamingEngine` is a class that represents a database engine.
 *  It provides methods to execute streaming queries from a database.
 *  @tparam F - The type of the context in which the database queries are executed.
 */
class SlickPgStreamingEngine[F[_]: Async](val db: Database, chunkSize: Int = 512) extends DBStreamingEngine[F] {

  /** The type of Slick queries that produce `T` */
  type QueryType[R] = SlickQuery[R]

  /**
   *  Executes a Slick query and returns the result as an `fs2.Stream[F, R]`.
   *
   *  @param query the Slick query to execute
   *  @return the query result as an `fs2.Stream[F, R]`
   */
  def runStreaming[R](query: QueryType[R]): fs2.Stream[F, R] = {
    val slickPublisher = db.stream(query.sql.as[R](query.getResult))
    slickPublisher.toStreamBuffered[F](chunkSize)
  }

}
