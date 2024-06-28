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

package za.co.absa.db.fadb.slick

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.SQLActionBuilder
import za.co.absa.db.fadb.DBSchema
import za.co.absa.db.fadb.slick.FaDbPostgresProfile.api._
import za.co.absa.db.fadb.slick.SlickFunction.SlickMultipleResultFunctionWithStatus
import za.co.absa.db.fadb.status.{FunctionStatus, Row}
import za.co.absa.db.fadb.status.handling.implementations.StandardStatusHandling

import scala.concurrent.ExecutionContext.Implicits.global


class SlickMultipleResultFunctionWithStatusIntegrationTests extends AnyFunSuite with SlickTest with ScalaFutures {

  class GetActorsByLastname(implicit override val schema: DBSchema, val dbEngine: SlickPgEngine)
    extends SlickMultipleResultFunctionWithStatus[GetActorsByLastnameQueryParameters, Option[Actor]]
      with StandardStatusHandling
      with OptionalActorSlickConverter {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("actor_id", "first_name", "last_name")

    override def sql(values: GetActorsByLastnameQueryParameters): SQLActionBuilder = {
      sql"""SELECT #$selectEntry FROM #$functionName(${values.lastName},${values.firstName}) #$alias;"""
    }
  }

  private val getActorsByLastname = new GetActorsByLastname()(Integration, new SlickPgEngine(db))

  test("Retrieving actors from database") {
    val expectedResultElem = Set(
      Right(Row(FunctionStatus(11, "OK, match on last name only"), Some(Actor(51, "Fred", "Weasley")))),
      Right(Row(FunctionStatus(11, "OK, match on last name only"), Some(Actor(52, "George", "Weasley"))))
    )

    val results = getActorsByLastname(GetActorsByLastnameQueryParameters("Weasley")).futureValue
    assert(results.toSet == expectedResultElem)
  }
}
