package za.co.absa.fadb.status.handling

import za.co.absa.fadb.FunctionStatusWithData
import za.co.absa.fadb.exceptions.StatusException

trait QueryStatusHandling {
  def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A]
}
