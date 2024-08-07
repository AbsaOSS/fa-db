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

package za.co.absa.db.fadb.slick

import slick.jdbc.{GetResult, PositionedResult, SQLActionBuilder}
import za.co.absa.db.fadb.status.{FailedOrRow, FunctionStatus, Row}
import za.co.absa.db.fadb.{Query, QueryWithStatus}

/**
 *  SQL query representation for Slick
 *  @param sql        - the SQL query in Slick format
 *  @param getResult  - function that converts the [[slick.jdbc.PositionedResult slick.PositionedResult]]
 *                    (the result of Slick execution) into the desired `R` type
 *  @tparam R         - the return type of the query
 */
class SlickQuery[R](val sql: SQLActionBuilder, val getResult: GetResult[R]) extends Query[R]

/**
 *  SQL query representation for Slick with status
 *  @param sql        - the SQL query in Slick format
 *  @param getResult  - function that converts the [[slick.jdbc.PositionedResult slick.PositionedResult]]
 *                    (the result of Slick execution) into the desired `R` type
 *  @tparam R         - the return type of the query
 */
class SlickQueryWithStatus[R](
  val sql: SQLActionBuilder,
  val getResult: GetResult[R],
  checkStatus: Row[PositionedResult] => FailedOrRow[PositionedResult]
) extends QueryWithStatus[PositionedResult, PositionedResult, R] {

  /**
   *  Processes the status of the query and returns the status with data
   *  @param initialResult - the initial result of the query
   *  @return data with status
   */
  override def processStatus(initialResult: PositionedResult): Row[PositionedResult] = {
    val status: Int = initialResult.<<
    val statusText: String = initialResult.<<
    Row(FunctionStatus(status, statusText), initialResult)
  }

  /**
   *  Converts the status with data to either a status exception or the data
   *  @param statusWithData - the status with data
   *  @return either a status exception or the data
   */
  override def toStatusExceptionOrData(
    statusWithData: Row[PositionedResult]
  ): FailedOrRow[R] = {
    checkStatus(statusWithData) match {
      case Left(statusException)  => Left(statusException)
      case Right(value) =>
        val status = value.functionStatus
        val data = getResult(value.data)
        Right(Row(status, data))
    }
  }

  /**
   *  Combines the processing of the status and the conversion of the status with data to either a status exception or the data
   *
   *  Note: GetResult processes data row by row.
   *
   *  @return the GetResult, that combines the processing of the status and the conversion of the status with data
   *  to either a status exception or the data
   */
  def getStatusExceptionOrData: GetResult[FailedOrRow[R]] = {
    GetResult(pr => processStatus(pr)).andThen(fs => toStatusExceptionOrData(fs))
  }
}
