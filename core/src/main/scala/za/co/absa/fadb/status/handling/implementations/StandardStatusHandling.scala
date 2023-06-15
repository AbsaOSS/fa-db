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

package za.co.absa.fadb.status.handling.implementations

import za.co.absa.fadb.exceptions.DBFailException
import za.co.absa.fadb.status.FunctionStatus
import za.co.absa.fadb.status.StatusException._
import za.co.absa.fadb.status.handling.StatusHandling

import scala.util.{Failure, Success, Try}

/**
  * A mix-in trait for [[za.co.absa.fadb.DBFunction DBFunction]] for standard handling of `status` and `statusText` fields.
  */
trait StandardStatusHandling extends StatusHandling {
  override protected def checkStatus(status: FunctionStatus): Try[FunctionStatus] = {
    status.statusCode / 10 match {
      case 1              => Success(status)
      case 2              => Failure(ServerMisconfigurationException(status))
      case 3              => Failure(DataConflictException(status))
      case 4              => Failure(DataNotFoundException(status))
      case 5 | 6 | 7 | 8  => Failure(ErrorInDataException(status))
      case 9              => Failure(OtherStatusException(status))
      case _              => Failure(DBFailException(s"Status out of range - with status: ${status.statusCode} and status text: '${status.statusText}'"))
    }
  }
}
