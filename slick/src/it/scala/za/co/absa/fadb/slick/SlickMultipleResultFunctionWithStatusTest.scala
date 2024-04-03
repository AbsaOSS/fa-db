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

package za.co.absa.fadb.slick

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.SQLActionBuilder
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._
import za.co.absa.fadb.slick.SlickFunction.SlickMultipleResultFunctionWithStatus
import za.co.absa.fadb.status.{FunctionStatus, FunctionStatusWithData}
import za.co.absa.fadb.status.aggregation.implementations.AggregateByFirstError
import za.co.absa.fadb.status.handling.implementations.StandardStatusHandling

import scala.concurrent.ExecutionContext.Implicits.global


class SlickMultipleResultFunctionWithStatusTest extends AnyFunSuite with SlickTest with ScalaFutures {

  class GetActorsByLastname(implicit override val schema: DBSchema, val dbEngine: SlickPgEngine)
    extends SlickMultipleResultFunctionWithStatus[GetActorsByLastnameQueryParameters, Option[Actor]]
      with StandardStatusHandling
      with AggregateByFirstError
      with OptionalActorSlickConverter {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("actor_id", "first_name", "last_name")

    override def sql(values: GetActorsByLastnameQueryParameters): SQLActionBuilder = {
      sql"""SELECT #$selectEntry FROM #$functionName(${values.lastName},${values.firstName}) #$alias;"""
    }
  }

  private val getActorsByLastname = new GetActorsByLastname()(Integration, new SlickPgEngine(db))

  test("Retrieving actors from database") {
    val expectedResultElem = Set(
      FunctionStatusWithData(FunctionStatus(11, "OK, match on last name only"), Some(Actor(51, "Fred", "Weasley"))),
      FunctionStatusWithData(FunctionStatus(11, "OK, match on last name only"), Some(Actor(52, "George", "Weasley")))
    )

    val results = getActorsByLastname(GetActorsByLastnameQueryParameters("Weasley")).futureValue
    val actualData = results match {
      case Left(_) => fail("should not be left")
      case Right(dataWithStatuses) => dataWithStatuses
    }
    assert(actualData.length == 2)
    assert(actualData.toSet == expectedResultElem)

  }
}
