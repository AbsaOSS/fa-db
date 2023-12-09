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
import za.co.absa.fadb.doobiedb.DoobieFunction.DoobieMultipleResultFunction

class DoobieMultipleResultFunctionTest extends AnyFunSuite with DoobieTest {

  class GetActors(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieMultipleResultFunction[GetActorsQueryParameters, Actor, IO] {

    override def sql(values: GetActorsQueryParameters)(implicit read: Read[Actor]): Fragment =
      sql"SELECT actor_id, first_name, last_name FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
  }

  private val getActors = new GetActors()(Runs, new DoobieEngine(transactor))

  test("DoobieTest") {
    val expectedResultElem = Actor(49, "Pavel", "Marek")
    val results = getActors(GetActorsQueryParameters(Some("Pavel"), Some("Marek"))).unsafeRunSync()
    assert(results.contains(expectedResultElem))
  }
}
