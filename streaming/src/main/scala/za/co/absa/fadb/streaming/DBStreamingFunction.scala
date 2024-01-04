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

import za.co.absa.fadb.{DBEngine, DBFunctionFabric, DBSchema}

import scala.language.higherKinds

/**
 *  [[DBStreamingFunction]] is an abstract class that represents a database function returning a stream of results.
 *
 *  @param functionNameOverride - Optional parameter to override the class name if it does not match the database function name.
 *  @param schema - The schema the function belongs to.
 *  @param dbStreamingEngine - The database engine that is supposed to execute the function (contains connection to the database).
 *  @tparam I - The type covering the input fields of the database function.
 *  @tparam R - The type covering the returned fields from the database function.
 *  @tparam E - The type of the [[DBStreamingEngine]] engine.
 *  @tparam F - The type of the context in which the database function is executed.
 */
abstract class DBStreamingFunction[I, R, E <: DBStreamingEngine[F], F[_]](functionNameOverride: Option[String] = None)(
  implicit override val schema: DBSchema,
  val dbStreamingEngine: E
) extends DBFunctionFabric(functionNameOverride) {

  // A constructor that takes only the mandatory parameters and uses default values for the optional ones
  def this()(implicit schema: DBSchema, dBEngine: E) = this(None)

  // A constructor that allows specifying the function name as a string, but not as an option
  def this(functionName: String)(implicit schema: DBSchema, dBEngine: E) = this(Some(functionName))

  /**
   *  Function to create the DB function call specific to the provided [[DBEngine]].
   *  Expected to be implemented by the DBEngine specific mix-in.
   *  @param values - The values to pass over to the database function.
   *  @return - The SQL query in the format specific to the provided [[DBEngine]].
   */
  protected def query(values: I): dbStreamingEngine.QueryType[R]

  /**
   * Executes the database function and returns stream of results
   * @param values The values to pass over to the database function
   * @return A stream of results from the database function
   */
  def apply(values: I): fs2.Stream[F, R] = dbStreamingEngine.runStreaming(query(values))

  /**
   * Executes the database function and returns stream of results. Allows to specify chunk size.
   * @param values The values to pass over to the database function
   * @param chunkSize The chunk size to use for the stream
   * @return A stream of results from the database function
   */
  def apply(values: I, chunkSize: Int): fs2.Stream[F, R] = {
    dbStreamingEngine.runStreamingWithChunkSize(query(values), chunkSize)
  }
}
