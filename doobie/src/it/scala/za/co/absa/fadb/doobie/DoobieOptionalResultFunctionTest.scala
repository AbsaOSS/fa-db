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
import doobie.implicits.toSqlInterpolator
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieOptionalResultFunction

class DoobieOptionalResultFunctionTest extends AnyFunSuite with DoobieTest {

  class GetActorById(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieOptionalResultFunction[Int, Actor, IO](id => Seq(fr"$id"))

  private val getActorById = new GetActorById()(Integration, new DoobieEngine(transactor))

  test("Retrieve actor by id from database") {
    val expectedResult = Some(Actor(49, "Pavel", "Marek"))
    val result = getActorById(49).unsafeRunSync()
    assert(expectedResult == result)
  }

}
