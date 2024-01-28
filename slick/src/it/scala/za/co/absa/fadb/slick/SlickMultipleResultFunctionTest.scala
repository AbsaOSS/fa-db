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

import cats.implicits.catsSyntaxApplicativeError
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.SlickFunction.SlickMultipleResultFunction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import za.co.absa.fadb.slick.FaDbPostgresProfile.api._

class SlickMultipleResultFunctionTest extends AnyFunSuite with SlickTest with ScalaFutures {

  class GetActors(implicit override val schema: DBSchema, val dbEngine: SlickPgEngine)
      extends SlickMultipleResultFunction[GetActorsQueryParameters, Actor] (
        values => {
//          throw new Exception("boom!")
//          Seq(s"#${values.firstName}::TEXT", s"#${values.firstName}::TEXT") // initial
//          Seq(sql"${values.firstName}::TEXT", sql"${values.firstName}::TEXT")
          Seq(
//            values.firstName.map(value => sql"#$value::TEXT").getOrElse(sql"NULL::TEXT"),
//            values.firstName.map(value => sql"$value"),
//            values.lastName.map(value => sql"#$value::TEXT").getOrElse(sql"NULL::TEXT")
//            values.lastName.map(value => sql"$value")
            sql"${values.firstName}",
            sql"${values.lastName}"
          )
        }
      )
      with ActorSlickConverter

  private val getActors = new GetActors()(Runs, new SlickPgEngine(db))

  test("Retrieving actors from database; handling exception") {
    val expectedResultElem = Actor(49, "Pavel", "Marek")
    val results = getActors(GetActorsQueryParameters(Some("Pavel"), Some("Marek")))//.handleErrorWith(_ => Future(Seq(expectedResultElem)))
    assert(results.futureValue.contains(expectedResultElem))
  }

  test("Retrieving actors from database; not handling exception") {
    assertThrows[Exception](getActors(GetActorsQueryParameters(Some("Pavel"), Some("Marek"))).futureValue)
  }
}
