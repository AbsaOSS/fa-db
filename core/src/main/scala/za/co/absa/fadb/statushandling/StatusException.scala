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

package za.co.absa.fadb.statushandling

import za.co.absa.fadb.exceptions.DBFailException

/**
  * Exception caused by status signaling a failure in DB function execution
  * @param status     - the status that caused the error
  * @param statusText - the status text explaining the status code
  */
class StatusException(val status: Int, statusText: String) extends DBFailException(statusText) {
  def statusText: String = getMessage
}

object StatusException {
  class ServerMisconfigurationException(status: Int, statusText: String) extends StatusException(status, statusText)

  class DataConflictException(status: Int, statusText: String) extends StatusException(status, statusText)

  class DataNotFoundException(status: Int, statusText: String) extends StatusException(status, statusText)

  class ErrorInDataException(status: Int, statusText: String) extends StatusException(status, statusText)

  class OtherStatusException(status: Int, statusText: String) extends StatusException(status, statusText)

}
