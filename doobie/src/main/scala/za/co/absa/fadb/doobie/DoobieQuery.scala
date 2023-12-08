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

package za.co.absa.fadb.doobie

import doobie.util.Read
import doobie.util.fragment.Fragment
import za.co.absa.fadb.exceptions.StatusException
import za.co.absa.fadb.status.FunctionStatus
import za.co.absa.fadb.{FunctionStatusWithData, Query, QueryWithStatus}

/**
 *  `DoobieQuery` is a class that extends `Query` with `R` as the result type.
 *  It uses Doobie's `Fragment` to represent SQL queries.
 *
 *  @param fragment the Doobie fragment representing the SQL query
 *  @param readR the `Read[R]` instance used to read the query result into `R`
 */
class DoobieQuery[R: Read](val fragment: Fragment)(implicit val readR: Read[R]) extends Query[R]

class DoobieQueryWithStatus[R](
  val fragment: Fragment,
  checkStatus: FunctionStatusWithData[R] => Either[StatusException, R]
)(implicit val readStatusWithDataR: Read[StatusWithData[R]]) extends QueryWithStatus[StatusWithData[R], R, R] {

  override def processStatus(initialResult: StatusWithData[R]): FunctionStatusWithData[R] =
    FunctionStatusWithData(FunctionStatus(initialResult.status, initialResult.status_text), initialResult.data)

  override def toStatusExceptionOrData(statusWithData: FunctionStatusWithData[R]): Either[StatusException, R] =
    checkStatus(statusWithData)
}
