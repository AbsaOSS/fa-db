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

import cats.implicits.catsSyntaxApplicativeError
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.SQLActionBuilder
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.SlickFunction.SlickMultipleResultFunction
import za.co.absa.fadb.tags.IntegrationTestTag

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._


class SlickMultipleResultFunctionTest extends AnyFunSuite with SlickTest with ScalaFutures {

  class GetActors(implicit override val schema: DBSchema, val dbEngine: SlickPgEngine)
    extends SlickMultipleResultFunction[GetActorsQueryParameters, Actor]
      with ActorSlickConverter {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("actor_id", "first_name", "last_name")

    override def sql(values: GetActorsQueryParameters): SQLActionBuilder = {
      sql"""SELECT #$selectEntry FROM #$functionName(${values.firstName},${values.lastName}) #$alias;"""
    }
  }

  private val getActors = new GetActors()(Integration, new SlickPgEngine(db))

  test("Retrieving actors from database", IntegrationTestTag) {
    val expectedResultElem = Actor(49, "Pavel", "Marek")
    val results = getActors(GetActorsQueryParameters(Some("Pavel"), Some("Marek")))
    assert(results.futureValue.contains(expectedResultElem))
  }
}
