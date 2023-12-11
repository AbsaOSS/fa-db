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
import doobie.Fragment
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.invariant.NonNullableColumnRead
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobiedb.DoobieFunction.DoobieSingleResultFunctionWithStatus
import za.co.absa.fadb.status.handling.implementations.StandardStatusHandling

class DoobieSingleResultFunctionWithStatusTest extends AnyFunSuite with DoobieTest {

  class CreateActor(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieSingleResultFunctionWithStatus[CreateActorRequestBody, Int, IO]
      with StandardStatusHandling {

    override def sql(values: CreateActorRequestBody)(implicit read: Read[StatusWithData[Int]]): Fragment = {
      sql"SELECT status, status_text, o_actor_id FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
    }
  }

  class ErrorIfNotOne(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieSingleResultFunctionWithStatus[Int, Int, IO]
      with StandardStatusHandling {

    override def sql(values: Int)(implicit read: Read[StatusWithData[Int]]): Fragment =
      sql"SELECT * FROM ${Fragment.const(functionName)}(${values})"
  }

  class ErrorIfNotOneWithStatus(functionNameOverride: String)(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieSingleResultFunctionWithStatus[Int, Option[Int], IO](Some(functionNameOverride))
      with StandardStatusHandling {

    override def sql(values: Int)(implicit read: Read[StatusWithData[Option[Int]]]): Fragment =
      sql"SELECT * FROM ${Fragment.const(functionName)}(${values})"
  }

  private val createActor = new CreateActor()(Runs, new DoobieEngine(transactor))
  private val errorIfNotOne = new ErrorIfNotOne()(Runs, new DoobieEngine(transactor))

  test("Creating actor within a function with status handling") {
    val requestBody = CreateActorRequestBody("Pavel", "Marek")
    val result = createActor(requestBody).unsafeRunSync()
    assert(result.isRight)
  }

  test("Successful function call with status handling") {
    val result = errorIfNotOne(1).unsafeRunSync()
    assert(result.isRight)
  }

  test("Unsuccessful function call with status handling. Asserting on error when Int not wrapped in Option") {
    // SQL `NULL` read at column 3 (JDBC type Integer) but mapping is to a non-Option type; use Option here. Note that JDBC column indexing is 1-based.
    assertThrows[NonNullableColumnRead](errorIfNotOne(2).unsafeRunSync())
  }

  test("Unsuccessful function call with status handling. Asserting on error when Int wrapped in Option") {
    val errorIfNotOne = new ErrorIfNotOneWithStatus("error_if_not_one")(Runs, new DoobieEngine(transactor))

    val result = errorIfNotOne(2).unsafeRunSync()
    assert(result.isLeft)
  }

  test("Successful function call with status handling. Asserting on success when Int wrapped in Option") {
    val errorIfNotOne = new ErrorIfNotOneWithStatus("error_if_not_one")(Runs, new DoobieEngine(transactor))

    val result = errorIfNotOne(1).unsafeRunSync()
    result match {
      case Left(_) => fail("should not be left")
      case Right(value) => assert(value.contains(1))
    }
  }
}
