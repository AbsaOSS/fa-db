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

package za.co.absa.fadb.statushandling

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.naming_conventions.{NamingConvention, SnakeCaseNaming}

import scala.util.{Failure, Success, Try}

class UserDefinedStatusHandlingSuite extends AnyFunSuite {
  test("") {
    class UserDefinedStatusHandlingForTest(val OKStatuses: Set[Integer]) extends UserDefinedStatusHandling {
      override def checkStatus(status: FunctionStatus): Try[FunctionStatus] = super.checkStatus(status)
      override def functionName: String = "Never needed"
      override def namingConvention: NamingConvention = SnakeCaseNaming.Implicits.namingConvention
    }

    val statusHandling = new UserDefinedStatusHandlingForTest(Set(200, 201))

    val oK = FunctionStatus(200, "OK")
    val alsoOK = FunctionStatus(201, "Also OK")
    val notOK = FunctionStatus(500, "Not OK")
    assert(statusHandling.checkStatus(oK) == Success(oK))
    assert(statusHandling.checkStatus(alsoOK) == Success(alsoOK))
    assert(statusHandling.checkStatus(notOK) == Failure(new StatusException(notOK)))
  }
}
