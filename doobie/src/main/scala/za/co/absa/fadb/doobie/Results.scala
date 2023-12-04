package za.co.absa.fadb.doobie

import za.co.absa.fadb.status.FunctionStatus

object Results {

  /**
   *  `SuccessfulResult` is a case class that represents a successful result of a function.
   *
   *  @param functionStatus the status of the function
   *  @param result the result of the function
   */
  case class SuccessfulResult[R](functionStatus: FunctionStatus, result: R)

  /**
   *  `FailedResult` is a case class that represents a failed result of a function.
   *
   *  @param functionStatus the status of the function
   *  @param failure the exception that caused the function to fail
   */
  case class FailedResult(functionStatus: FunctionStatus, failure: Throwable)

  /**
   *  `ResultWithStatus` is a type alias for `Either[FailedResult, SuccessfulResult[R]]`.
   *  It represents a result of a function that can either be successful or failed.
   */
  type ResultWithStatus[R] = Either[FailedResult, SuccessfulResult[R]]
}
