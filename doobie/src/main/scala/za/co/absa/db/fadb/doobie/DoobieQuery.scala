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

package za.co.absa.db.fadb.doobie

import doobie.util.Read
import doobie.util.fragment.Fragment
import za.co.absa.db.fadb.exceptions.StatusException
import za.co.absa.db.fadb.status.{FailedOrRow, FunctionStatus, Row}
import za.co.absa.db.fadb.{Query, QueryWithStatus}

/**
 *  `DoobieQuery` is a class that extends `Query` with `R` as the result type.
 *  It uses Doobie's `Fragment` to represent SQL queries.
 *
 *  @param fragment the Doobie fragment representing the SQL query
 *  @param readR the `Read[R]` instance used to read the query result into `R`
 */
class DoobieQuery[R](val fragment: Fragment)(implicit val readR: Read[R]) extends Query[R]

/**
 *  `DoobieQueryWithStatus` is a class that extends `QueryWithStatus` with `R` as the result type.
 *  It uses Doobie's `Fragment` to represent SQL queries.
 *
 *  @param fragment the Doobie fragment representing the SQL query
 *  @param checkStatus the function to check the status of the query
 *  @param readStatusWithData the `Read[StatusWithData[R]]` instance used to read the query result into `StatusWithData[R]`
 */
class DoobieQueryWithStatus[R](
  val fragment: Fragment,
  checkStatus: FunctionStatus => Option[StatusException]
)(implicit val readStatusWithData: Read[StatusWithData[R]])
    extends QueryWithStatus[StatusWithData[R], Option[R], R] {

  /*
   * Processes the status of the query and returns the status with data
   * @param initialResult - the initial result of the query
   * @return data with status
   */
  override def processStatus(initialResult: StatusWithData[R]): Row[Option[R]] =
    Row(FunctionStatus(initialResult.status, initialResult.statusText), initialResult.data)

  /*
   * Converts the status with data to either a status exception or the data
   * @param statusWithData - the status with data
   * @return either a status exception or the data
   */
  override def toStatusExceptionOrData(statusWithData: Row[Option[R]]): FailedOrRow[R] =
    checkStatus(statusWithData.functionStatus).toLeft(
      statusWithData.data.getOrElse(throw new IllegalStateException("Status is OK but data is missing"))
    ).map(r => Row(statusWithData.functionStatus, r))

}
