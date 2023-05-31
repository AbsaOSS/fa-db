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

package za.co.absa.fadb.statushandling.fadbstandard

import za.co.absa.fadb.exceptions.DBFailException
import za.co.absa.fadb.statushandling.StatusHandling
import za.co.absa.fadb.statushandling.StatusException._

import scala.util.{Failure, Success, Try}

/**
  * A mix in trait for [[DBFunction]] for standard handling of `status` and `status_text` fields.
  */
trait StandardStatusHandling extends StatusHandling {
  override protected def checkStatus(status: Integer, statusText: String): Try[Unit] = {
    status / 10 match {
      case 1              => Success(Unit)
      case 2              => Failure(new ServerMisconfigurationException(status, statusText))
      case 3              => Failure(new DataConflictException(status, statusText))
      case 4              => Failure(new DataNotFoundException(status, statusText))
      case 5 | 6 | 7 | 8  => Failure(new ErrorInDataException(status, statusText))
      case 9              => Failure(new OtherStatusException(status, statusText))
      case _              => Failure(DBFailException(s"Status out of range - with status: $status and status text: '$statusText'"))
    }
  }
}
