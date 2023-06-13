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

package za.co.absa.fadb.statushandling.fadbstandard

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.exceptions.DBFailException
import za.co.absa.fadb.naming_conventions.{NamingConvention, SnakeCaseNaming}
import za.co.absa.fadb.statushandling.{FunctionStatus, StatusException}
import za.co.absa.fadb.statushandling.StatusException._

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

class StandardStatusHandlingSuite extends AnyFunSuite {
  test("Verify checkStatus error mapping") {
    class StandardStatusHandlingForTest extends StandardStatusHandling {
      override def checkStatus(status: FunctionStatus): Try[FunctionStatus] = super.checkStatus(status)
      override def checkStatus(status: Integer, statusText: String): Try[FunctionStatus] = super.checkStatus(status, statusText)
      override def functionName: String = "Never needed"
      override def namingConvention: NamingConvention = SnakeCaseNaming.Implicits.namingConvention
    }

    def assertCheckStatusFailure[F <: StatusException](status: Int, statusText: String)
                                                      (implicit classTag: ClassTag[F], checker: StandardStatusHandlingForTest): Unit = {

      val failure = intercept[F] {
        checker.checkStatus(status, statusText).get
      }
      assert(failure.status == FunctionStatus(status, statusText))
    }
    implicit val standardStatusHandling: StandardStatusHandlingForTest = new StandardStatusHandlingForTest

    assert(standardStatusHandling.checkStatus(FunctionStatus(10, "OK")) == Success(FunctionStatus(10, "OK")))
    assertCheckStatusFailure[ServerMisconfigurationException](21, "Server is wrongly set up")
    assertCheckStatusFailure[DataConflictException](31, "Referenced data does not allow execution of the request")
    assertCheckStatusFailure[DataNotFoundException](42, "Detail record not found")
    assertCheckStatusFailure[ErrorInDataException](58, "Some incorrect data")
    assertCheckStatusFailure[ErrorInDataException](69, "Missing value for field XYZ")
    assertCheckStatusFailure[ErrorInDataException](73, "Value ABC is out of range")
    assertCheckStatusFailure[ErrorInDataException](84, "Json value of field FF is missing property PPP")
    assertCheckStatusFailure[OtherStatusException](95, "This is a special error")
    
    val status = 101
    val statusText = "Server is wrongly set up"
    val expectedFailure = Failure(DBFailException(s"Status out of range - with status: $status and status text: '${statusText}'"))
    assert(standardStatusHandling.checkStatus(101, "Server is wrongly set up") == expectedFailure)
  }
}
