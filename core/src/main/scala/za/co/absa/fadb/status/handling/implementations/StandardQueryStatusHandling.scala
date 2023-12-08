package za.co.absa.fadb.status.handling.implementations

import za.co.absa.fadb.FunctionStatusWithData
import za.co.absa.fadb.exceptions._
import za.co.absa.fadb.status.handling.QueryStatusHandling

/**
 * `StandardQueryStatusHandling` is a trait that extends the [[QueryStatusHandling]] interface.
 * It provides a standard implementation for checking the status of a function invocation.
 */
trait StandardQueryStatusHandling extends QueryStatusHandling {

  /**
   * Checks the status of a function invocation.
   * @param statusWithData - The status of the function invocation with the data returned by the function.
   * @tparam A - The type of the data returned by the function.
   * @return Either the data returned by the function or an exception.
   */
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
