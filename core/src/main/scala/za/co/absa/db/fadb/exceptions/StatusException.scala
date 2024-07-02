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

package za.co.absa.db.fadb.exceptions

import za.co.absa.db.fadb.status.FunctionStatus

/**
 *  Represents an exception that is returned when the function status is not successful.
 */
sealed abstract class StatusException(val status: FunctionStatus) extends Exception(status.statusText)

/**
 *  Represents an exception that is returned when there is a server misconfiguration.
 */
final case class ServerMisconfigurationException(override val status: FunctionStatus) extends StatusException(status)

/**
 *  Represents an exception that is returned when there is a data conflict.
 */
final case class DataConflictException(override val status: FunctionStatus) extends StatusException(status)

/**
 *  Represents an exception that is returned when data is not found.
 */
final case class DataNotFoundException(override val status: FunctionStatus) extends StatusException(status)

/**
 *  Represents an exception that is returned when there is an error in data.
 */
final case class ErrorInDataException(override val status: FunctionStatus) extends StatusException(status)

/**
 *  Represents an exception that is returned for other statuses.
 */
final case class OtherStatusException(override val status: FunctionStatus) extends StatusException(status)

/**
 *  Represents an exception that is returned when the status is out of range.
 */
final case class StatusOutOfRangeException(override val status: FunctionStatus) extends StatusException(status)
