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
  * @param status     - represent the status information returned from the function call
  */
class StatusException(val status:FunctionStatus) extends DBFailException(status.statusText) {

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: StatusException => (other.status == status)  && (getClass == other.getClass)
      case _ => false
    }
  }
}

object StatusException {

  def apply(status: FunctionStatus): StatusException = new StatusException(status)
  def apply(status: Int, statusText: String): StatusException = new StatusException(FunctionStatus(status, statusText))

  class ServerMisconfigurationException(status:FunctionStatus) extends StatusException(status)

  class DataConflictException(status:FunctionStatus) extends StatusException(status)

  class DataNotFoundException(status:FunctionStatus) extends StatusException(status)

  class ErrorInDataException(status:FunctionStatus) extends StatusException(status)

  class OtherStatusException(status:FunctionStatus) extends StatusException(status)

  object ServerMisconfigurationException {
    def apply(status: FunctionStatus): ServerMisconfigurationException = new ServerMisconfigurationException(status)
    def apply(status: Int, statusText: String): ServerMisconfigurationException = new ServerMisconfigurationException(FunctionStatus(status, statusText))
  }

  object DataConflictException {
    def apply(status: FunctionStatus): DataConflictException = new DataConflictException(status)
    def apply(status: Int, statusText: String): DataConflictException = new DataConflictException(FunctionStatus(status, statusText))
  }

  object DataNotFoundException {
    def apply(status: FunctionStatus): DataNotFoundException = new DataNotFoundException(status)
    def apply(status: Int, statusText: String): DataNotFoundException = new DataNotFoundException(FunctionStatus(status, statusText))
  }

  object ErrorInDataException {
    def apply(status: FunctionStatus): ErrorInDataException = new ErrorInDataException(status)
    def apply(status: Int, statusText: String): ErrorInDataException = new ErrorInDataException(FunctionStatus(status, statusText))
  }

  object OtherStatusException {
    def apply(status: FunctionStatus): OtherStatusException = new OtherStatusException(status)
    def apply(status: Int, statusText: String): OtherStatusException = new OtherStatusException(FunctionStatus(status, statusText))
  }

}
