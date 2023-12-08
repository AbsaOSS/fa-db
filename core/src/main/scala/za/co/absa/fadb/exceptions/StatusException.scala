package za.co.absa.fadb.exceptions

import za.co.absa.fadb.status.FunctionStatus

sealed abstract class StatusException(val status: FunctionStatus) extends Exception(status.statusText)

final case class ServerMisconfigurationException(override val status: FunctionStatus) extends StatusException(status)
final case class DataConflictException(override val status: FunctionStatus) extends StatusException(status)
final case class DataNotFoundException(override val status: FunctionStatus) extends StatusException(status)
final case class ErrorInDataException(override val status: FunctionStatus) extends StatusException(status)
final case class OtherStatusException(override val status: FunctionStatus) extends StatusException(status)
final case class StatusOutOfRangeException(override val status: FunctionStatus) extends StatusException(status)
