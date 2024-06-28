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

package za.co.absa.db.fadb

import za.co.absa.db.fadb.exceptions.StatusException

package object status {

  /**
    *  Class represents the status of calling a fa-db function (if it supports status that is)
    */
  case class FunctionStatus(statusCode: Int, statusText: String)

  /**
    *  Represents a function status with data.
    *  @param functionStatus the function status
    *  @param data the data of one row (barring the status fields)
    *  @tparam D the type of the data
    */
  case class Row[D](functionStatus: FunctionStatus, data: D)

  /**
    *  This is a representation of a single row returned from a DB function with processed status information.
    *
    *  Note: D here represents a single row reduced by status-related columns, i.e. a type of data.
    */
  type FailedOrRow[D] = Either[StatusException, Row[D]]

  /**
    * This is a representation of multiple rows returned from a DB function with processed status information,
    * with error statuses aggregated to a single one.
    *
    * Note: D here represents a single row reduced by status-related columns, i.e. a type of data.
    */
  type FailedOrRows[D] = Either[StatusException, Seq[Row[D]]]
}
