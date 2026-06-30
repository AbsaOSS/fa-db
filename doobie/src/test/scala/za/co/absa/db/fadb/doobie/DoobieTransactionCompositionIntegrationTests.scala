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
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.db.fadb.DBSchema
import za.co.absa.db.fadb.doobie.DoobieFunction.{DoobieMultipleResultFunction, DoobieOptionalResultFunction, DoobieSingleResultFunctionWithStatus}
import za.co.absa.db.fadb.status.FailedOrRow
import za.co.absa.db.fadb.status.handling.implementations.StandardStatusHandling
import za.co.absa.db.fadb.testing.classes.DoobieTest

class DoobieTransactionCompositionIntegrationTests extends AnyFunSuite with DoobieTest {

  private val engine = new DoobieEngine(transactor)

  class CreateActor(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieSingleResultFunctionWithStatus[CreateActorRequestBody, Int, IO](
      values => Seq(fr"${values.firstName}", fr"${values.lastName}")
    )
    with StandardStatusHandling {
    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("o_actor_id")
  }

  class GetActorById(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieOptionalResultFunction[Int, Actor, IO](id => Seq(fr"$id"))

  class GetActors(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieMultipleResultFunction[GetActorsQueryParameters, Actor, IO](
      values => Seq(fr"${values.firstName}", fr"${values.lastName}")
    )

  private val createActor = new CreateActor()(Integration, engine)
  private val getActorById = new GetActorById()(Integration, engine)
  private val getActors = new GetActors()(Integration, engine)

  /** Lifts a FailedOrRow into ConnectionIO — raises StatusException on Left, unwraps data on Right */
  private def liftFailedOrRow[R](result: FailedOrRow[R]): ConnectionIO[R] = result match {
    case Right(row) => row.data.pure[ConnectionIO]
    case Left(ex)   => doobie.FC.raiseError(ex)
  }

  test("Compose two function calls into a single transaction using toConnectionIO") {
    val uniqueFirst = s"TxTest_${System.currentTimeMillis()}"
    val uniqueLast = "CompositionTest"

    val program: ConnectionIO[(Int, Option[Actor])] = for {
      created <- createActor.toConnectionIO(CreateActorRequestBody(uniqueFirst, uniqueLast))
      actorId <- liftFailedOrRow(created.head)
      found   <- getActorById.toConnectionIOOptional(actorId)
    } yield (actorId, found)

    val (actorId, foundActor) = engine.runConnectionIO(program).unsafeRunSync()

    assert(actorId > 0)
    assert(foundActor.isDefined)
    assert(foundActor.get.firstName == uniqueFirst)
    assert(foundActor.get.lastName == uniqueLast)
  }

  test("Compose using toConnectionIOSingle convenience method") {
    val uniqueFirst = s"TxSingle_${System.currentTimeMillis()}"
    val uniqueLast = "SingleTest"

    val program: ConnectionIO[Actor] = for {
      _      <- createActor.toConnectionIOSingle(CreateActorRequestBody(uniqueFirst, uniqueLast))
      actors <- getActors.toConnectionIO(GetActorsQueryParameters(Some(uniqueFirst), Some(uniqueLast)))
    } yield actors.head

    val actor = engine.runConnectionIO(program).unsafeRunSync()

    assert(actor.firstName == uniqueFirst)
    assert(actor.lastName == uniqueLast)
  }

  test("Compose multiple creates in a single transaction") {
    val timestamp = System.currentTimeMillis()
    val lastName = s"BatchTest_$timestamp"

    val program: ConnectionIO[Unit] = for {
      _ <- createActor.toConnectionIOSingle(CreateActorRequestBody("BatchActor1", lastName))
      _ <- createActor.toConnectionIOSingle(CreateActorRequestBody("BatchActor2", lastName))
      _ <- createActor.toConnectionIOSingle(CreateActorRequestBody("BatchActor3", lastName))
    } yield ()

    engine.runConnectionIO(program).unsafeRunSync()

    // Verify all actors exist — they were created in a single transaction
    val actors = getActors(GetActorsQueryParameters(None, Some(lastName))).unsafeRunSync()
    assert(actors.size >= 3)
  }

  test("Transaction rolls back all operations on failure") {
    val uniqueFirst = s"Rollback_${System.currentTimeMillis()}"
    val uniqueLast = "RollbackTest"

    val program: ConnectionIO[Unit] = for {
      _ <- createActor.toConnectionIOSingle(CreateActorRequestBody(uniqueFirst, uniqueLast))
      _ <- doobie.FC.raiseError[Unit](new RuntimeException("Simulated failure after create"))
    } yield ()

    val result = engine.runConnectionIO(program).attempt.unsafeRunSync()
    assert(result.isLeft, "Transaction should have failed")

    // The actor should NOT exist because the transaction was rolled back
    val actors = getActors(GetActorsQueryParameters(Some(uniqueFirst), Some(uniqueLast))).unsafeRunSync()
    assert(actors.isEmpty, "Actor should not exist after transaction rollback")
  }

  test("Individual apply() calls still work independently (backward compatibility)") {
    val uniqueFirst = s"Compat_${System.currentTimeMillis()}"
    val uniqueLast = "CompatTest"

    val result = createActor(CreateActorRequestBody(uniqueFirst, uniqueLast)).unsafeRunSync()
    assert(result.isRight)

    val actorId = result.toOption.get.data
    val found = getActorById(actorId).unsafeRunSync()
    assert(found.isDefined)
    assert(found.get.firstName == uniqueFirst)
  }
}
