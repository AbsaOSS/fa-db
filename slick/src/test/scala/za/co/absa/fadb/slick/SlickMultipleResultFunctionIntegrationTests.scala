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
import za.co.absa.fadb.slick.SlickFunction.SlickMultipleResultFunction

import scala.concurrent.ExecutionContext.Implicits.global


class SlickMultipleResultFunctionIntegrationTests extends AnyFunSuite with SlickTest with ScalaFutures {

  class GetActors(implicit override val schema: DBSchema, val dbEngine: SlickPgEngine)
    extends SlickMultipleResultFunction[GetActorsQueryParameters, Actor]
      with ActorSlickConverter {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("actor_id", "first_name", "last_name")

    override def sql(values: GetActorsQueryParameters): SQLActionBuilder = {
      sql"""SELECT #$selectEntry FROM #$functionName(${values.firstName},${values.lastName}) #$alias;"""
    }
  }

  private val getActors = new GetActors()(Integration, new SlickPgEngine(db))

  test("Retrieving actors from database") {
    val expectedResultElem = Set(
      Actor(51, "Fred", "Weasley"),
      Actor(52, "George", "Weasley"),
    )
    val results = getActors(GetActorsQueryParameters(lastName=Some("Weasley"), firstName=None)).futureValue
    assert(results.toSet == expectedResultElem)
  }
}
