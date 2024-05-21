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

package za.co.absa.fadb.doobie

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.Fragment
import doobie.implicits.toSqlInterpolator
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieMultipleResultFunctionWithStatus
import za.co.absa.fadb.status.aggregation.implementations.ByMajorityErrorsStatusAggregator
import za.co.absa.fadb.status.handling.implementations.StandardStatusHandling
import za.co.absa.fadb.status.{FunctionStatus, Row}

class DoobieMultipleResultFunctionWithStatusIntegrationTests extends AnyFunSuite with DoobieTest {

  private val getActorsByLastnameQueryFragments: GetActorsByLastnameQueryParameters => Seq[Fragment] = {
    values => Seq(fr"${values.lastName}", fr"${values.firstName}")
  }

  class GetActorsByLastname(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      // Option[Actor] because: Actor might not exist, and the function would return only status info without actor data
      extends DoobieMultipleResultFunctionWithStatus[GetActorsByLastnameQueryParameters, Option[Actor], IO](getActorsByLastnameQueryFragments)
        with StandardStatusHandling
        with ByMajorityErrorsStatusAggregator {
    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("actor_id", "first_name", "last_name")
  }

  private val getActorsByLastname = new GetActorsByLastname()(Integration, new DoobieEngine(transactor))

  test("Retrieving multiple actors from database, lastName match") {
    val expectedResultElem = Set(
      Row(FunctionStatus(11, "OK, match on last name only"), Some(Actor(51, "Fred", "Weasley"))),
      Row(FunctionStatus(11, "OK, match on last name only"), Some(Actor(52, "George", "Weasley"))),
    )

    val results = getActorsByLastname(GetActorsByLastnameQueryParameters("Weasley")).unsafeRunSync()
    val actualData = results match {
      case Left(_) => fail("should not be left")
      case Right(dataWithStatuses) => dataWithStatuses
    }
    assert(actualData.length == expectedResultElem.size)
    assert(actualData.toSet == expectedResultElem)
  }

  test("Retrieving single actor from database, full match") {
    val expectedResultElem = Row(
      FunctionStatus(12, "OK, full match"), Some(Actor(50, "Liza", "Simpson"))
    )

    val results = getActorsByLastname(GetActorsByLastnameQueryParameters("Simpson", Some("Liza"))).unsafeRunSync()
    val actualData = results match {
      case Left(_) => fail("should not be left")
      case Right(dataWithStatuses) => dataWithStatuses
    }

    assert(actualData.length == 1)
    assert(actualData.head == expectedResultElem)
  }

  test("Retrieving single actor from database, lastname match") {
    val expectedResultElem = Row(
      FunctionStatus(11, "OK, match on last name only"), Some(Actor(50, "Liza", "Simpson"))
    )

    val results = getActorsByLastname(GetActorsByLastnameQueryParameters("Simpson")).unsafeRunSync()
    val actualData = results match {
      case Left(_) => fail("should not be left")
      case Right(dataWithStatuses) => dataWithStatuses
    }

    assert(actualData.length == 1)
    assert(actualData.head == expectedResultElem)
  }

  test("Retrieving non-existing actor from database, no match") {
    val results = getActorsByLastname(GetActorsByLastnameQueryParameters("TotallyNonExisting!")).unsafeRunSync()
    results match {
      case Left(err) =>
        assert(err.status.statusText == "No actor found")
        assert(err.status.statusCode == 41)
      case Right(_) => fail("should not be right")
    }
  }
}
