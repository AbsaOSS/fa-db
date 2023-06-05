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
import za.co.absa.fadb.DBFunctionFabric
import za.co.absa.fadb.naming_conventions.{NamingConvention, SnakeCaseNaming}

class StatusHandlingSuite extends AnyFunSuite {
  test("Fields to select filled with default values") {
    trait FooDBFunction extends DBFunctionFabric {
      override def fieldsToSelect: Seq[String] = Seq("Alpha", "beta")
    }

    class StatusHandlingForTest extends FooDBFunction with StatusHandling {
      override def functionName: String = "Never needed"
      override def namingConvention: NamingConvention = SnakeCaseNaming.Implicits.namingConvention

      override protected def checkStatus(status: FunctionStatus) = throw new Exception("Should never get here")
      override def fieldsToSelect: Seq[String] = super.fieldsToSelect
    }

    val statusHandling = new StatusHandlingForTest
    assert(statusHandling.fieldsToSelect == Seq("status", "status_text", "alpha", "beta"))

  }
}
