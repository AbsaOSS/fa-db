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


import slick.jdbc.{GetResult, PositionedResult}
import za.co.absa.fadb.{DBEngine, Query}

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._

import scala.language.higherKinds

class SlickPgEngine(val db: Database) extends DBEngine {

  //override type QueryType[R] = SlickQuery[R]

  override protected def run[R, Q <: SlickQuery[R]](query: Q): Future[Seq[R]] = {
    // It can be expected that a GetResult will be passed into the run function as converter.
    // Unfortunately it has to be recreated to be used by Slick
    val slickAction = query.sql.as[query.RESULT](query.getResult)
    db.run(slickAction)
  }

}
