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

package za.co.absa.db.fadb.doobie

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.invariant.NonNullableColumnRead
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.db.fadb.DBSchema
import za.co.absa.db.fadb.doobie.DoobieFunction.DoobieSingleResultFunctionWithStatus
import za.co.absa.db.fadb.exceptions.StatusException
import za.co.absa.db.fadb.status.handling.implementations.StandardStatusHandling
import za.co.absa.db.fadb.testing.classes.DoobieTest

class DoobieSingleResultFunctionWithStatusIntegrationTests extends AnyFunSuite with DoobieTest {

  class CreateActor(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieSingleResultFunctionWithStatus[CreateActorRequestBody, Int, IO] (
        values => {
          throw new Throwable("boom")
          Seq(fr"${values.firstName}", fr"${values.lastName}")
        }
      )
      with StandardStatusHandling {
    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("o_actor_id")
  }

  class ErrorIfNotOne(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieSingleResultFunctionWithStatus[Int, Int, IO] (params => Seq(fr"$params"))
      with StandardStatusHandling {
    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("input_value")
  }

  class ErrorIfNotOneOption(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieSingleResultFunctionWithStatus[Int, Option[Int], IO] (
      params => Seq(fr"$params"),
      Some("error_if_not_one")
    )
      with StandardStatusHandling {
    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("input_value")
  }

  private val createActor = new CreateActor()(Integration, new DoobieEngine(transactor))
  private val errorIfNotOne = new ErrorIfNotOne()(Integration, new DoobieEngine(transactor))
  private val errorIfNotOneOption = new ErrorIfNotOneOption()(Integration, new DoobieEngine(transactor))

  test("Creating actor within a function with status handling") {
    val requestBody = CreateActorRequestBody("Pavel", "Marek")
    val result = createActor(requestBody).handleErrorWith(_ => IO(Right[StatusException, Int](125))).unsafeRunSync()
    assert(result == Right(125))
  }

  test("Unsuccessful function call with status handling") {
    val result = errorIfNotOne(2).unsafeRunSync()
    assert(result.isLeft)
  }

  test("Successful function call with status handling") {
    val result = errorIfNotOne(1).unsafeRunSync()
    result match {
      case Left(_) => fail("should not be left")
      case Right(value) => assert(value.data === 1)
    }
  }

  test("backwards compatibility with already Optioned result #133)") {
    assert(errorIfNotOneOption(2).unsafeRunSync().isLeft)
    errorIfNotOneOption(1).unsafeRunSync() match {
      case Left(_) => fail("should not be left")
      case Right(value) => assert(value.data === Some(1))
    }
  }

}
