package za.co.absa.fadb.status.handling.implementations

import za.co.absa.fadb.FunctionStatusWithData
import za.co.absa.fadb.exceptions.{DataConflictException, DataNotFoundException, ErrorInDataException, OtherStatusException, ServerMisconfigurationException, StatusException, StatusOutOfRangeException}
import za.co.absa.fadb.status._
import za.co.absa.fadb.status.handling.QueryStatusHandling

trait StandardQueryStatusHandling extends QueryStatusHandling {
  override def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A] = {
    val functionStatus = statusWithData.functionStatus
    functionStatus.statusCode / 10 match {
      case 1             => Right(statusWithData.data)
      case 2             => Left(ServerMisconfigurationException(functionStatus))
      case 3             => Left(DataConflictException(functionStatus))
      case 4             => Left(DataNotFoundException(functionStatus))
      case 5 | 6 | 7 | 8 => Left(ErrorInDataException(functionStatus))
      case 9             => Left(OtherStatusException(functionStatus))
      case _             => Left(StatusOutOfRangeException(functionStatus))
    }
  }
}
