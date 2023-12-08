package za.co.absa.fadb.status.handling.implementations

import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers.{a, convertToAnyShouldWrapper}
import za.co.absa.fadb.FunctionStatusWithData
import za.co.absa.fadb.exceptions._
import za.co.absa.fadb.status.FunctionStatus

class StandardQueryStatusHandlingTest extends AnyFunSuiteLike {

  private val standardQueryStatusHandling = new StandardQueryStatusHandling {}

  test("checkStatus should return Right when status code is in the range 10-19") {
    for (statusCode <- 10 to 19) {
      val functionStatus = FunctionStatus(statusCode, "Success")
      val statusWithData = FunctionStatusWithData(functionStatus, "Data")
      val result = standardQueryStatusHandling.checkStatus(statusWithData)
      result shouldBe Right("Data")
    }
  }

  test("checkStatus should return Left with ServerMisconfigurationException when status code is in the range 20-29") {
    for (statusCode <- 20 to 29) {
      val functionStatus = FunctionStatus(statusCode, "Server Misconfiguration")
      val statusWithData = FunctionStatusWithData(functionStatus, "Data")
      val result = standardQueryStatusHandling.checkStatus(statusWithData)
      result shouldBe Left(ServerMisconfigurationException(functionStatus))
    }
  }

  test("checkStatus should return Left with DataConflictException when status code is in the range 30-39") {
    for (statusCode <- 30 to 39) {
      val functionStatus = FunctionStatus(statusCode, "Data Conflict")
      val statusWithData = FunctionStatusWithData(functionStatus, "Data")
      val result = standardQueryStatusHandling.checkStatus(statusWithData)
      result shouldBe Left(DataConflictException(functionStatus))
    }
  }

  test("checkStatus should return Left with DataNotFoundException when status code is in the range 40-49") {
    for (statusCode <- 40 to 49) {
      val functionStatus = FunctionStatus(statusCode, "Data Not Found")
      val statusWithData = FunctionStatusWithData(functionStatus, "Data")
      val result = standardQueryStatusHandling.checkStatus(statusWithData)
      result shouldBe Left(DataNotFoundException(functionStatus))
    }
  }

  test("checkStatus should return Left with ErrorInDataException when status code is in the range 50-89") {
    for (statusCode <- 50 to 89) {
      val functionStatus = FunctionStatus(statusCode, "Error in Data")
      val statusWithData = FunctionStatusWithData(functionStatus, "Data")
      val result = standardQueryStatusHandling.checkStatus(statusWithData)
      result shouldBe Left(ErrorInDataException(functionStatus))
    }
  }

  test("checkStatus should return Left with OtherStatusException when status code is in the range 90-99") {
    for (statusCode <- 90 to 99) {
      val functionStatus = FunctionStatus(statusCode, "Other Status")
      val statusWithData = FunctionStatusWithData(functionStatus, "Data")
      val result = standardQueryStatusHandling.checkStatus(statusWithData)
      result shouldBe Left(OtherStatusException(functionStatus))
    }
  }

  test("checkStatus should return Left with StatusOutOfRangeException when status code is not in any known range") {
    for (statusCode <- 0 to 9) {
      val functionStatus = FunctionStatus(statusCode, "Out of range")
      val statusWithData = FunctionStatusWithData(functionStatus, "Data")
      val result = standardQueryStatusHandling.checkStatus(statusWithData)
      result shouldBe Left(StatusOutOfRangeException(functionStatus))
    }

    for (statusCode <- 100 to 110) {
      val functionStatus = FunctionStatus(statusCode, "Out of range")
      val statusWithData = FunctionStatusWithData(functionStatus, "Data")
      val result = standardQueryStatusHandling.checkStatus(statusWithData)
      result shouldBe Left(StatusOutOfRangeException(functionStatus))
    }
  }

}
