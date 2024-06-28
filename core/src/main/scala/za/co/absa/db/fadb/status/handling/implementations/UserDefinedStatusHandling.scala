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

package za.co.absa.db.fadb.status.handling.implementations

import za.co.absa.db.fadb.exceptions.OtherStatusException
import za.co.absa.db.fadb.status.handling.StatusHandling
import za.co.absa.db.fadb.status.{FailedOrRow, Row}

/**
 *  Trait represents user defined status handling
 */
trait UserDefinedStatusHandling extends StatusHandling {
  def OKStatuses: Set[Integer]

  override def checkStatus[D](statusWithData: Row[D]): FailedOrRow[D] =
    if (OKStatuses.contains(statusWithData.functionStatus.statusCode)) {
      Right(statusWithData)
    } else {
      Left(OtherStatusException(statusWithData.functionStatus))
    }
}
