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

package za.co.absa.db.fadb.status.aggregation.implementations

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.fadb.exceptions._
import za.co.absa.db.fadb.status.{FunctionStatus, Row}

class ByFirstErrorStatusAggregatorUnitTests extends AnyFunSuiteLike {

  private val aggregateByFirstErrorUnderTest = new ByFirstErrorStatusAggregator {}

  test("aggregate should return Empty Seq in Right for an empty Sequence") {
    val testData = Seq.empty
    val expectedAggData = Right(Seq.empty)

    val actualAggData = aggregateByFirstErrorUnderTest.aggregate(testData)
    assert(actualAggData == expectedAggData)
  }

  test("aggregate should return Seq with data in Right for a Sequence with data (no errors") {
    val rawTestData = Seq(
      Row(FunctionStatus(10, "Ok"), ("FirstName1", "SecondName1")),
      Row(FunctionStatus(10, "Ok"), ("FirstName2", "SecondName2")),
      Row(FunctionStatus(10, "Ok"), ("FirstName3", "SecondName3")),
    )
    val testData = rawTestData.map(Right(_)) // wrap so that it's Seq of Eithers with data
    val expectedAggData = Right(rawTestData) // wrap so that it's Either of Seq with data

    val actualAggData = aggregateByFirstErrorUnderTest.aggregate(testData)
    assert(actualAggData == expectedAggData)
  }

  test("aggregate should return a single Left only, when there is single error status code, no data") {
    val testData = Seq(
      Left(DataNotFoundException(FunctionStatus(42, "Data not found"))),
    )
    val expectedAggData = Left(DataNotFoundException(FunctionStatus(42, "Data not found")))

    val actualAggData = aggregateByFirstErrorUnderTest.aggregate(testData)
    assert(actualAggData == expectedAggData)
  }

  test("aggregate should return a single Left only, when there are multiple error status codes, no data") {
    val testData = Seq(
      Left(DataNotFoundException(FunctionStatus(42, "Data not found"))),
      Left(DataNotFoundException(FunctionStatus(43, "Data not found another"))),
    )
    val expectedAggData = Left(DataNotFoundException(FunctionStatus(42, "Data not found")))

    val actualAggData = aggregateByFirstErrorUnderTest.aggregate(testData)
    assert(actualAggData == expectedAggData)
  }

  test("aggregate should return a single Left only, when there is a single error status code along with data") {
    val testData = Seq(
      Left(DataNotFoundException(FunctionStatus(42, "Data not found"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName1", "SecondName1"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName2", "SecondName2"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName3", "SecondName3"))),
    )
    val expectedAggData = Left(DataNotFoundException(FunctionStatus(42, "Data not found")))

    val actualAggData = aggregateByFirstErrorUnderTest.aggregate(testData)
    assert(actualAggData == expectedAggData)
  }

  test("aggregate should return a single Left only, when there are multiple error status codes along with data") {
    val testData = Seq(
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName1", "SecondName1"))),
      Left(DataNotFoundException(FunctionStatus(42, "Data not found"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName2", "SecondName2"))),
      Left(DataNotFoundException(FunctionStatus(43, "Data not found another"))),
      Right(Row(FunctionStatus(10, "Ok"), ("FirstName3", "SecondName3"))),
    )
    val expectedAggData = Left(DataNotFoundException(FunctionStatus(42, "Data not found")))

    val actualAggData = aggregateByFirstErrorUnderTest.aggregate(testData)
    assert(actualAggData == expectedAggData)
  }

}
