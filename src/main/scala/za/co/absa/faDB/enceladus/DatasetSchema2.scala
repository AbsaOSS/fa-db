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
import za.co.absa.faDB.Slick.{SlickExecutor, SlickFunction}
import za.co.absa.faDB.namingConventions.SnakeCaseNaming.Implicits.namingConvention
import DatasetSchema2._
import slick.jdbc.GetResult
import za.co.absa.faDB.DBFunction._
import slick.jdbc.PostgresProfile.api._
import za.co.absa.faDB.enceladus.DatasetSchema.Schema
import za.co.absa.faDB.exceptions.DBFailException

import java.sql.Timestamp
import scala.concurrent.Future

class DatasetSchema2(executor: SlickExecutor) extends DBSchema2(executor) {

  private implicit val schema: DBSchema2[ExecutorEngineType] = this
  val addSchema = new AddSchema
  val getSchema = new GetSchema
  val listSchemas = new ListSchemas
}


object DatasetSchema2 {
  type ExecutorEngineType = Database

  case class SchemaInput(schemaName: String,
                         schemaVersion: Int,
                         schemaDescription: Option[String],
                         fields: Option[String],
                         userName: String)

  case class Schema(idSchemaVersion: Long,
                    schemaName: String,
                    schemaVersion: Int,
                    schemaDescription: Option[String],
                    fields: Option[String],
                    createdBy: String,
                    createdWhen: Timestamp,
                    updatedBy: String,
                    updatedWhen: Timestamp,
                    lockedBy: Option[String],
                    lockedWhen: Option[Timestamp],
                    deletedBy: Option[String],
                    deletedWhen: Option[Timestamp])

  case class SchemaHeader(schemaName: String, schemaLatestVersion: Int)

  private implicit val SchemaHeaderImplicit: GetResult[SchemaHeader] = GetResult(r => {SchemaHeader(r.<<, r.<<)})
  private implicit val GetSchemaImplicit: GetResult[Schema] = GetResult(r => {
    val status: Int = r.<<
    val statusText: String = r.<<
    if (status != 200) {
      throw DBFailException(status, statusText)
    }
    Schema(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<)
  })

  final class AddSchema(implicit schema: DBSchema2[ExecutorEngineType])
    extends DBUniqueFunction[ExecutorEngineType, SchemaInput, Long](schema)
    with SlickFunction {

    override protected def queryFunction(values: SchemaInput): QueryFunction[ExecutorEngineType, Long] = {
      val gr:GetResult[Long] = GetResult(r => {
        val status: Int = r.<<
        val statusText: String = r.<<
        if (status != 201) throw DBFailException(status, statusText)
        r.<<
      })

      val sql =
        sql"""SELECT A.status, A.status_text, A.id_schema_version
             FROM #$functionName(${values.schemaName}, ${values.schemaVersion}, ${values.schemaDescription},
                ${values.fields}::JSONB, ${values.userName}
             ) A;"""

      makeQueryFunction(sql)(gr)
    }
  }

  final class GetSchema(implicit schema: DBSchema2[ExecutorEngineType])
    extends DBUniqueFunction[ExecutorEngineType, (String, Option[Int]), Schema](schema)
    with SlickFunction {

    def apply(id: Long): Future[Schema] = {
      val sql =
        sql"""SELECT A.*
             FROM #$functionName($id) A;"""
      schema.unique(makeQueryFunction[Schema](sql))
    }

    override protected def queryFunction(values: (String, Option[Int])): QueryFunction[ExecutorEngineType, Schema] = {
      val sql =
        sql"""SELECT A.*
              FROM #$functionName(${values._1}, ${values._2}) A;"""

      makeQueryFunction[Schema](sql)
    }
  }

  final class ListSchemas(implicit schema: DBSchema2[ExecutorEngineType])
    extends DBSeqFunction[ExecutorEngineType, Boolean, SchemaHeader](schema)()
    with SlickFunction {

    override protected def queryFunction(values: Boolean): QueryFunction[ExecutorEngineType, SchemaHeader] = {
      val sql =
             sql"""SELECT A.schema_name, A.schema_latest_version
                   FROM #$functionName($values) as A;"""
      makeQueryFunction[SchemaHeader](sql)
    }
  }
}
