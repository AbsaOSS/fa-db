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

import cats.Semigroup
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxSemigroup
import doobie.Fragment
import doobie.implicits.toSqlInterpolator
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieMultipleResultFunction

class DoobieMultipleResultFunctionTest extends AnyFunSuite with DoobieTest {

  implicit def toFragmentsFunctionSemigroup[T]: Semigroup[T => Seq[Fragment]] = {
    (f1: T => Seq[Fragment], f2: T => Seq[Fragment]) => (params: T) => f1(params) ++ f2(params)
  }

  private val firstNameFragment: GetActorsQueryParameters => Seq[Fragment] = params => Seq(fr"${params.firstName}")
  private val lastNameFragment: GetActorsQueryParameters => Seq[Fragment] = params => Seq(fr"${params.lastName}")

  private val combinedQueryFragments: GetActorsQueryParameters => Seq[Fragment] =
    params => firstNameFragment(params) ++ lastNameFragment(params)

  // using Semigroup's combine method, |+| is syntactical sugar for combine method
  private val combinedUsingSemigroup = firstNameFragment |+| lastNameFragment

  // not combined, defined as one function
  private val getActorsQueryFragments: GetActorsQueryParameters => Seq[Fragment] = {
    values => Seq(fr"${values.firstName}", fr"${values.lastName}")
  }

  class GetActors(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
      extends DoobieMultipleResultFunction[GetActorsQueryParameters, Actor, IO](combinedUsingSemigroup)

  private val getActors = new GetActors()(Runs, new DoobieEngine(transactor))

  test("Retrieving actor from database") {
    val expectedResultElem = Actor(49, "Pavel", "Marek")
    val results = getActors(GetActorsQueryParameters(Some("Pavel"), Some("Marek"))).unsafeRunSync()
    assert(results.contains(expectedResultElem))
  }
}
