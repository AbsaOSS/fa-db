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
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import za.co.absa.fadb.DBSchema

trait DoobieTest {
  case class Actor(actorId: Int, firstName: String, lastName: String)
  case class GetActorsQueryParameters(firstName: Option[String], lastName: Option[String])
  case class CreateActorRequestBody(firstName: String, lastName: String)

  import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits._
  object Integration extends DBSchema

  protected val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/movies",
    "postgres",
    "postgres",
  )
}
