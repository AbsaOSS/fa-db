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
import za.co.absa.fadb.status.handling.StandardQueryStatusHandling
import za.co.absa.fadb.status.{FunctionStatus, StatusException}
import za.co.absa.fadb.{FunctionStatusWithData, Query, QueryWithStatus}

/**
 * SQL query representation for Slick
 * @param sql        - the SQL query in Slick format
 * @param getResult  - the converting function, that converts the [[slick.jdbc.PositionedResult slick.PositionedResult]] (the result of Slick
 *                   execution) into the desire `R` type
 * @tparam R         - the return type of the query
 */
class SlickQuery[R](val sql: SQLActionBuilder, val getResult: GetResult[R]) extends Query[R]

// QueryStatusHandling has to be mixed-in for the checkStatus method implementation
class SlickQueryWithStatus[R](val sql: SQLActionBuilder, val getResult: GetResult[R])
  extends QueryWithStatus[PositionedResult, PositionedResult, R] with StandardQueryStatusHandling {

  override def processStatus(initialResult: PositionedResult): FunctionStatusWithData[PositionedResult] = {
    val status: Int = initialResult.<<
    val statusText: String = initialResult.<<
    FunctionStatusWithData(FunctionStatus(status, statusText), initialResult)
  }

  override def toStatusExceptionOrData(statusWithData: FunctionStatusWithData[PositionedResult]): Either[StatusException, R] = {
    checkStatus(statusWithData) match {
      case Left(statusException) => Left(statusException)
      case Right(value) => Right(getResult(value))
    }
  }

  def getStatusExceptionOrData: GetResult[Either[StatusException, R]] = {
    GetResult(pr => processStatus(pr)).andThen(fs => toStatusExceptionOrData(fs))
  }
}
