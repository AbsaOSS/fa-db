package za.co.absa.fadb.status.handling

import za.co.absa.fadb.FunctionStatusWithData
import za.co.absa.fadb.exceptions.StatusException

/**
 * `QueryStatusHandling` is a base trait that defines the interface for handling the status of a function invocation.
 * It provides a method to check the status of a function invocation with data.
 */
trait QueryStatusHandling {
  /**
   * Checks the status of a function invocation.
   * @param statusWithData - The status of the function invocation with data.
   * @return Either a [[StatusException]] if the status code indicates an error, or the data if the status code is successful.
   */
  def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A]
}
