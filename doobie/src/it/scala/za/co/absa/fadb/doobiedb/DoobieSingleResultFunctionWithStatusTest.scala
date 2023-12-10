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

  private val createActor = new CreateActor()(Runs, new DoobieEngine(transactor))
  private val errorIfNotOne = new ErrorIfNotOne()(Runs, new DoobieEngine(transactor))

  test("DoobieTest with status handling") {
    val requestBody = CreateActorRequestBody("Pavel", "Marek")
    val result = createActor(requestBody).unsafeRunSync()
    assert(result.isRight)
    println(result)
  }

  test("error if not one with status handling") {
    val result = errorIfNotOne(1).unsafeRunSync()
    assert(result.isRight)
    println(result)
  }

  test("error if not one with status handling - error") {
    // throws null exception
    // SQL `NULL` read at column 3 (JDBC type Integer) but mapping is to a non-Option type; use Option here. Note that JDBC column indexing is 1-based.
    assertThrows[NonNullableColumnRead](errorIfNotOne(2).unsafeRunSync())
  }

  test("error if not one with status handling - error - null wrapped in option") {
    class ErrorIfNotOne(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieSingleResultFunctionWithStatus[Int, Option[Int], IO]
        with StandardStatusHandling {

      override def sql(values: Int)(implicit read: Read[StatusWithData[Option[Int]]]): Fragment =
        sql"SELECT * FROM ${Fragment.const(functionName)}(${values})"
    }

    val errorIfNotOne = new ErrorIfNotOne()(Runs, new DoobieEngine(transactor))

    // does not throw because null is wrapped in option
    val result = errorIfNotOne(2).unsafeRunSync()
    assert(result.isLeft)
  }

  test("error if not one with status handling - success - null wrapped in option") {
    class ErrorIfNotOne(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieSingleResultFunctionWithStatus[Int, Option[Int], IO]
        with StandardStatusHandling {

      override def sql(values: Int)(implicit read: Read[StatusWithData[Option[Int]]]): Fragment =
        sql"SELECT * FROM ${Fragment.const(functionName)}(${values})"
    }

    val errorIfNotOne = new ErrorIfNotOne()(Runs, new DoobieEngine(transactor))

    val result = errorIfNotOne(1).unsafeRunSync()
    result match {
      case Left(_) => fail("should not be left")
      case Right(value) => assert(value.contains(1))
    }
  }
}
