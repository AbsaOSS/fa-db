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

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.SQLActionBuilder
import za.co.absa.fadb.DBFunction.DBOptionalResultFunction
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class SlickOptionalResultFunctionTest extends AnyFunSuite with SlickTest {

  class GetActorById(implicit override val schema: DBSchema, val dbEngine: SlickPgEngine)
    extends DBOptionalResultFunction[Int, Actor, SlickPgEngine, Future]
      with SlickFunction[Int, Actor]
      with ActorSlickConverter {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("actor_id", "first_name", "last_name")

    override protected def sql(values: Int): SQLActionBuilder = {
      sql"""SELECT #$selectEntry FROM #$functionName($values) #$alias;"""
    }
  }

  private val getActorById = new GetActorById()(Runs, new SlickPgEngine(db))

  test("SlickTest") {
    val expectedResultElem = Some(Actor(49, "Pavel", "Marek"))
    val results = getActorById(49)
    assert(Await.result(results, 5.seconds) == expectedResultElem)
  }
}
