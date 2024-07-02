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

package za.co.absa.db.fadb.status.aggregation

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.db.fadb.exceptions._
import za.co.absa.db.fadb.status.{FunctionStatus, Row}

class StatusAggregatorUnitTests extends AnyFunSuite {

  test("gatherExceptions should return empty Seq on empty input") {
    val testData = Seq.empty
    val expectedGatheredExceptions = Seq.empty

    val actualGatheredExceptions = StatusAggregator.gatherExceptions(testData)
    assert(actualGatheredExceptions == expectedGatheredExceptions)
  }

  test("gatherExceptions should gather exceptions") {
    val testData = Seq(
      Left(DataNotFoundException(FunctionStatus(42, "Data not found"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName1", "SecondName1"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName2", "SecondName2"))),
      Left(ErrorInDataException(FunctionStatus(50, "Some data error"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName3", "SecondName3"))),
    )
    val expectedGatheredExceptions = Seq(
      DataNotFoundException(FunctionStatus(42, "Data not found")),
      ErrorInDataException(FunctionStatus(50, "Some data error"))
    )

    val actualGatheredExceptions = StatusAggregator.gatherExceptions(testData)
    assert(actualGatheredExceptions == expectedGatheredExceptions)
  }

  test("gatherDataWithStatuses should return empty Seq on empty input") {
    val testData = Seq.empty
    val expectedGatheredExceptions = Seq.empty

    val actualGatheredExceptions = StatusAggregator.gatherDataWithStatuses(testData)
    assert(actualGatheredExceptions == expectedGatheredExceptions)
  }

  test("gatherDataWithStatuses should gather exceptions") {
    val testData = Seq(
      Left(DataNotFoundException(FunctionStatus(42, "Data not found"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName1", "SecondName1"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName2", "SecondName2"))),
      Left(ErrorInDataException(FunctionStatus(50, "Some data error"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName3", "SecondName3"))),
    )
    val expectedGatheredData = Seq(
      Row(FunctionStatus(10, "Ok"), ("FirstName1", "SecondName1")),
      Row(FunctionStatus(10, "Ok"), ("FirstName2", "SecondName2")),
      Row(FunctionStatus(10, "Ok"), ("FirstName3", "SecondName3")),
    )

    val actualGatheredData = StatusAggregator.gatherDataWithStatuses(testData)
    assert(actualGatheredData == expectedGatheredData)
  }

}
