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
import io.circe.Json
import io.circe.syntax.EncoderOps
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.db.fadb.DBSchema
import za.co.absa.db.fadb.doobie.DoobieFunction.{DoobieMultipleResultFunction, DoobieSingleResultFunction}
import za.co.absa.db.fadb.testing.classes.DoobieTest

import za.co.absa.db.fadb.doobie.postgres.circe.implicits.jsonOrJsonbArrayGet

class JsonArrayIntegrationTests extends AnyFunSuite with DoobieTest {

  class InsertActorsJson(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieSingleResultFunction[List[Actor], Unit, IO] (
      values => {
        val actorsAsJsonList = values.map(_.asJson)
        Seq(
          {
            import za.co.absa.db.fadb.doobie.postgres.circe.implicits.jsonArrayPut
            fr"$actorsAsJsonList"
          },
          {
            import za.co.absa.db.fadb.doobie.postgres.circe.implicits.jsonbArrayPut
            fr"$actorsAsJsonList"
          }
        )
      }
    )

  class RetrieveActorsJson(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieMultipleResultFunction[Int, List[Json], IO] (
      values => Seq(fr"$values")
    )

  class RetrieveActorsJsonb(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieMultipleResultFunction[Int, List[Json], IO] (
      values => Seq(fr"$values")
    )

  private val insertActorsJson = new InsertActorsJson()(Integration, new DoobieEngine(transactor))

  test("Retrieve Actors from json[] and jsonb[] columns"){
    val expectedActors = List(Actor(1, "John", "Doe"), Actor(2, "Jane", "Doe"))
    insertActorsJson(expectedActors).unsafeRunSync()

    val retrieveActorsJson = new RetrieveActorsJson()(Integration, new DoobieEngine(transactor))
    val actualActorsJson = retrieveActorsJson(2).unsafeRunSync()
    assert(expectedActors == actualActorsJson.head.map(_.as[Actor]).map(_.toTry.get))

    val retrieveActorsJsonb = new RetrieveActorsJsonb()(Integration, new DoobieEngine(transactor))
    val actualActorsJsonb = retrieveActorsJsonb(2).unsafeRunSync()
    assert(expectedActors == actualActorsJsonb.head.map(_.as[Actor]).map(_.toTry.get))
  }

}
