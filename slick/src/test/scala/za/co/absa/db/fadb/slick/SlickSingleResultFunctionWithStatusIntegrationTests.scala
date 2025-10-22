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

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.time.{Second, Span}
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.db.fadb.DBSchema
import za.co.absa.db.fadb.slick.FaDbPostgresProfile.api._
import za.co.absa.db.fadb.slick.SlickFunction.SlickSingleResultFunctionWithStatus
import za.co.absa.db.fadb.status.handling.implementations.StandardStatusHandling
import za.co.absa.db.fadb.testing.classes.SlickTest

import scala.concurrent.ExecutionContext.Implicits.global

class SlickSingleResultFunctionWithStatusIntegrationTests extends AnyFunSuite with SlickTest with ScalaFutures {
  class CreateActor(implicit schema: DBSchema, dbEngine: SlickPgEngine)
      extends SlickSingleResultFunctionWithStatus[CreateActorRequestBody, Int]
      with StandardStatusHandling {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("o_actor_id")

    override protected def sql(values: CreateActorRequestBody): SQLActionBuilder =
      sql"""SELECT #$selectEntry FROM #$functionName(${values.firstName},${values.lastName}) #$alias;"""

    override protected def slickConverter: GetResult[Int] = GetResult(r => r.<<)
  }

  private val createActor = new CreateActor()(Integration, new SlickPgEngine(db))

  test("Creating actor with status handling") {
    val requestBody = CreateActorRequestBody("Separated", "TestUser")
    val timeout = Timeout(Span(3.0D, Second))
    val result = createActor(requestBody).futureValue(timeout)
    assert(result.isRight)
  }
}
