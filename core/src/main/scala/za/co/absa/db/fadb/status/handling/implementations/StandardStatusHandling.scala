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

import za.co.absa.db.fadb.exceptions._
import za.co.absa.db.fadb.status.FunctionStatus
import za.co.absa.db.fadb.status.handling.StatusHandling

/**
 *  `StandardStatusHandling` is a trait that extends the `StatusHandling` interface.
 *  It provides a standard implementation for checking the status of a function invocation.
 */
trait StandardStatusHandling extends StatusHandling {

  /**
   *  Checks the status of a function invocation.
   */
  override def checkStatus(functionStatus: FunctionStatus): Option[StatusException] = {
    functionStatus.statusCode / 10 match {
      case 1             => None
      case 2             => Some(ServerMisconfigurationException(functionStatus))
      case 3             => Some(DataConflictException(functionStatus))
      case 4             => Some(DataNotFoundException(functionStatus))
      case 5 | 6 | 7 | 8 => Some(ErrorInDataException(functionStatus))
      case 9             => Some(OtherStatusException(functionStatus))
      case _             => Some(StatusOutOfRangeException(functionStatus))
    }
  }
}
