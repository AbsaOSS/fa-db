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

import slick.jdbc.{GetResult, PositionedResult, SQLActionBuilder}
import za.co.absa.fadb.exceptions.StatusException
import za.co.absa.fadb.status.FunctionStatus
import za.co.absa.fadb.{FunctionStatusWithData, Query, QueryWithStatus}

/**
 *  SQL query representation for Slick
 *  @param sql        - the SQL query in Slick format
 *  @param getResult  - function that converts the [[slick.jdbc.PositionedResult]]
 *                    (the result of Slick execution) into the desired `R` type
 *  @tparam R         - the return type of the query
 */
class SlickQuery[R](val sql: SQLActionBuilder, val getResult: GetResult[R]) extends Query[R]

/**
 *  SQL query representation for Slick with status
 *  @param sql        - the SQL query in Slick format
 *  @param getResult  - function that converts the [[slick.jdbc.PositionedResult]]
 *                    (the result of Slick execution) into the desired `R` type
 *  @tparam R         - the return type of the query
 */
class SlickQueryWithStatus[R](
  val sql: SQLActionBuilder,
  val getResult: GetResult[R],
  checkStatus: FunctionStatusWithData[PositionedResult] => Either[StatusException, PositionedResult]
) extends QueryWithStatus[PositionedResult, PositionedResult, R] {

  /**
   * Processes the status of the query and returns the status with data
   * @param initialResult - the initial result of the query
   * @return the status with data
   */
  override def processStatus(initialResult: PositionedResult): FunctionStatusWithData[PositionedResult] = {
    val status: Int = initialResult.<<
    val statusText: String = initialResult.<<
    FunctionStatusWithData(FunctionStatus(status, statusText), initialResult)
  }

  /**
   * Converts the status with data to either a status exception or the data
   * @param statusWithData - the status with data
   * @return either a status exception or the data
   */
  override def toStatusExceptionOrData(
    statusWithData: FunctionStatusWithData[PositionedResult]
  ): Either[StatusException, R] = {
    checkStatus(statusWithData) match {
      case Left(statusException) => Left(statusException)
      case Right(value)          => Right(getResult(value))
    }
  }

  /**
   * Combines the processing of the status and the conversion of the status with data to either a status exception or the data
   * @return the GetResult, that combines the processing of the status and the conversion of the status with data
   * to either a status exception or the data
   */
  def getStatusExceptionOrData: GetResult[Either[StatusException, R]] = {
    GetResult(pr => processStatus(pr)).andThen(fs => toStatusExceptionOrData(fs))
  }
}
