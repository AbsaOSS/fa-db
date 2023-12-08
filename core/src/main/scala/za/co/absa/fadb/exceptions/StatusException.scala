package za.co.absa.fadb.exceptions

import za.co.absa.fadb.status.FunctionStatus

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
