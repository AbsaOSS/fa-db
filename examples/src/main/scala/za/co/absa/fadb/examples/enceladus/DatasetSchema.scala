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

package za.co.absa.fadb.examples.enceladus

import za.co.absa.fadb.{DBSchema}
import za.co.absa.fadb.slick.{SlickPgExecutor, SlickPgFunction}
import za.co.absa.fadb.naming_conventions.SnakeCaseNaming.Implicits.namingConvention
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.DBFunction._
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import scala.concurrent.Future
import DatasetSchema._
import za.co.absa.fadb.statushandling.StatusException

class DatasetSchema(executor: SlickPgExecutor) extends DBSchema(executor) {

  private implicit val schema: DBSchema[ExecutorEngineType] = this
  val addSchema = new AddSchema
  val getSchema = new GetSchema
  val listSchemas = new ListSchemas
}


object DatasetSchema {
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
      throw new StatusException(status, statusText)
    }
    Schema(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<)
  })

  final class AddSchema(implicit schema: DBSchema[ExecutorEngineType])
    extends DBUniqueFunction[ExecutorEngineType, SchemaInput, Long](schema)
    with SlickPgFunction[SchemaInput, Long] {


    override protected def sqlToCallFunction(values: SchemaInput): SQLActionBuilder = {
      sql"""SELECT A.status, A.status_text, A.id_schema_version
             FROM #$functionName(${values.schemaName}, ${values.schemaVersion}, ${values.schemaDescription},
                ${values.fields}::JSONB, ${values.userName}
             ) A;"""
    }

    override protected def resultConverter: GetResult[Long] = {
      val gr:GetResult[Long] = GetResult(r => {
        val status: Int = r.<<
        val statusText: String = r.<<
        if (status != 201) throw new StatusException(status, statusText)
        r.<<
      })
      gr
    }
  }

  final class GetSchema(implicit schema: DBSchema[ExecutorEngineType])
    extends DBUniqueFunction[ExecutorEngineType, (String, Option[Int]), Schema](schema)
    with SlickPgFunction[(String, Option[Int]), Schema] {

    def apply(id: Long): Future[Schema] = {
      val sql =
        sql"""SELECT A.*
             FROM #$functionName($id) A;"""

      schema.unique(makeQueryFunction(sql)(resultConverter))
    }

    override protected def sqlToCallFunction(values: (String, Option[Int])): SQLActionBuilder = {
      sql"""SELECT A.*
              FROM #$functionName(${values._1}, ${values._2}) A;"""
    }

    override protected def resultConverter: GetResult[Schema] = DatasetSchema.GetSchemaImplicit
  }

  final class ListSchemas(implicit schema: DBSchema[ExecutorEngineType])
    extends DBSeqFunction[ExecutorEngineType, Boolean, SchemaHeader](schema)
    with SlickPgFunction[Boolean, SchemaHeader] {

    override def apply(values: Boolean = false): Future[Seq[SchemaHeader]] = super.apply(values)

    override protected def sqlToCallFunction(values: Boolean): SQLActionBuilder = {
      sql"""SELECT A.schema_name, A.schema_latest_version
                   FROM #$functionName($values) as A;"""
    }

    override protected def resultConverter: GetResult[SchemaHeader] = DatasetSchema.SchemaHeaderImplicit
  }
}
