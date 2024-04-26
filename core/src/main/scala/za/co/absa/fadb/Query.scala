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

import za.co.absa.fadb.status.{FailedOrRow, Row}

/**
 *  The basis for all query types of [[DBEngine]] implementations
 *  @tparam R - the return type of the query
 */
trait Query[R]

/**
 *  The basis for all query types of [[DBEngine]] implementations with status
 *  @tparam A - the initial result type of the query (a row basically, having status-related columns as well)
 *  @tparam B - the intermediate result type of the query (a row without status columns, i.e. data only)
 *  @tparam R - the final return type of the query (final version of result, depending on the needs, might be the same as B)
 */
trait QueryWithStatus[A, B, R] {

  /**
   *  Processes the status of the query and returns the status with data
   *  @param initialResult - the initial result of the query
   *  @return the status with data
   */
  def processStatus(initialResult: A): Row[B]

  /**
   *  Converts the status with data to either a status exception or the data
   *  @param statusWithData - the status with data
   *  @return either a status exception or the data
   */
  def toStatusExceptionOrData(statusWithData: Row[B]): FailedOrRow[R]

  /**
   *  Returns the result of the query or a status exception
   *  @param initialResult - the initial result of the query
   *  @return the result of the query or a status exception
   */
  def getResultOrException(initialResult: A): FailedOrRow[R] =
    toStatusExceptionOrData(processStatus(initialResult))
}
