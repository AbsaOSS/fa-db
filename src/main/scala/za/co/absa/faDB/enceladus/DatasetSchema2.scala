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

package za.co.absa.faDB.enceladus

import slick.jdbc.JdbcBackend.Database
import za.co.absa.faDB.DBSchema2
import za.co.absa.faDB.Slick.SlickExecutor
import za.co.absa.faDB.namingConventions.SnakeCaseNaming._
import DatasetSchema2._
import za.co.absa.faDB.DBFunction.DBSetFunction

import scala.concurrent.Future

class DatasetSchema2(executor: SlickExecutor) extends DBSchema2(executor) {

  private implicit val schema: DBSchema2[Database] = this
//  val addSchema = new AddSchema
//  val getSchema = new GetSchema(this)
  val listSchemas = new ListSchemas
}


object DatasetSchema2 {
  final class ListSchemas(implicit schema: DatasetSchema) extends DBSetFunction[Database ,Boolean, SchemaHead] {
    override def apply(values: Boolean = false): Future[Seq[SchemaHead]] = {


      val sqlX =
        sql"""SELECT A.schema_name, A.schema_latest_version
             FROM #$functionName($values) A;"""

      val slickQuery = new SlickQuery {
        override def sql =  sqlX
        override def rConf[R] = {
          if
            GetResult(r => {SchemaHead(r.<<, r.<<)})
        }
      }
      schema.execute(slickQuery)
    }
  }
}
