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

package za.co.absa.fadb.doobiedb

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobiedb.DoobieFunction.DoobieOptionalResultFunction

class DoobieOptionalResultFunctionTest extends AnyFunSuite with DoobieTest {

  class GetActorById(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieOptionalResultFunction[Int, Actor, IO] {

    override def sql(values: Int)(implicit read: Read[Actor]): Fragment =
      sql"SELECT actor_id, first_name, last_name FROM ${Fragment.const(functionName)}($values)"
  }

  private val createActor = new GetActorById()(Runs, new DoobieEngine(transactor))

  test("DoobieTest") {
    val expectedResult = Some(Actor(49, "Pavel", "Marek"))
    val result = createActor(49).unsafeRunSync()
    assert(expectedResult == result)
  }

}
