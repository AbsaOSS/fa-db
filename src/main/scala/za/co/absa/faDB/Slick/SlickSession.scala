/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.faDB.Slick

import slick.jdbc.{GetResult, JdbcBackend}
import slick.jdbc.JdbcBackend.Database
import za.co.absa.faDB.{DBSession, Executor}

import scala.concurrent.Future

class SlickSession(val db: Database) extends DBSession[SlickQuery, GetResult[_]]{

//  override def executeQuery[R](query: SlickQuery)(implicit convertor: GetResult[R]): Future[Seq[R]] = {
//    val x = query.sql.as[R](query.rConf)
//    db.run(x)
// }
  override def executeQuery[R](query: SlickQuery)(implicit convertor: GetResult[_]): Future[Seq[R]] =  {
    ???
//    val x = query.sql.as[R](convertor)
//    db.run(x)
 }
}

object SlickSession {
  def apply(fromConfig: String): SlickSession = {
    new SlickSession(Database.forConfig(fromConfig))
  }
}


class SlickExecutor(db: Database) extends Executor[Database] {
  override def run[R](fnc: Database =>  Future[Seq[R]]): Future[Seq[R]] = {
    fnc(db)
  }
}