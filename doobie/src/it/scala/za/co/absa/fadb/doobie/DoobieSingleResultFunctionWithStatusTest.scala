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
import doobie.util.Read
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieSingleResultFunctionWithStatus

class DoobieSingleResultFunctionWithStatusTest extends AnyFunSuite with DoobieTest {

  class CreateActor(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieSingleResultFunctionWithStatus[CreateActorRequestBody, Int, IO] {

    override def sql(values: CreateActorRequestBody)(implicit read: Read[StatusWithData[Int]]): Fragment = {
      sql"SELECT status, status_text, o_actor_id FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
    }
  }

  private val createActor = new CreateActor()(Runs, new DoobieEngine(transactor))

  test("DoobieTest with status handling") {
    val requestBody = CreateActorRequestBody("Pavel", "Marek")
    val result = createActor(requestBody).unsafeRunSync()
    assert(result.isRight)
  }
}
