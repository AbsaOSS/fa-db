/*
 * Copyright 2022ABSA Group Limited
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

package za.co.absa.fadb.status

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.exceptions._

class StatusExceptionUnitTest extends AnyFunSuite {
  test("Test equals - when they are the same") {
    val statusException = DataConflictException(FunctionStatus(10, "OK"))
    val otherStatusException = DataConflictException(FunctionStatus(10, "OK"))

    assert(statusException == otherStatusException)
  }

  test("Test equals - when they are different") {
    val statusException = DataNotFoundException(FunctionStatus(10, "OK"))
    val otherStatusException = DataNotFoundException(FunctionStatus(10, "Hello"))
    val anotherStatusException = DataNotFoundException(FunctionStatus(11, "OK"))

    assert(statusException != otherStatusException)
    assert(statusException != anotherStatusException)
  }
}
