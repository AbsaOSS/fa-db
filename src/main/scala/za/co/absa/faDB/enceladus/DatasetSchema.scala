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

import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.GetResult
import za.co.absa.faDB.DBFunction.{DBSeqFunction, DBValueFunction}
import za.co.absa.faDB.enceladus.DatasetSchema.{AddSchema, GetSchema, ListSchemas}
import za.co.absa.faDB.{DBSchema, DBSession}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlStreamingAction
import za.co.absa.faDB.exceptions.DBFailException

import java.sql.Timestamp

import za.co.absa.faDB.namingConventions.SnakeCaseNaming.Implicits.namingConvention


class DatasetSchema(session: DBSession) extends DBSchema(session) {
  private val db = Database.forConfig(session.connection)

  val addSchema = new AddSchema(this)
  val getSchema = new GetSchema(this)
  val listSchemas = new ListSchemas(this)

  def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[R] = {
    db.run(a)
  }
}

object DatasetSchema {
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
  case class SchemaHead(schemaName: String, schemaLatestVersion: Int)

  private implicit val SchemaHeadImplicit: GetResult[SchemaHead] = GetResult(r => {SchemaHead(r.<<, r.<<)})
  private implicit val GetSchemaImplicit: GetResult[Schema] = GetResult(r => {
    val status: Int = r.<<
    val statusText: String = r.<<
    if (status != 200) {
      throw DBFailException(status, statusText)
    }
    Schema(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<)
  })

  final class ListSchemas(schema: DBSchema) extends DBSeqFunction[Boolean, SchemaHead](schema) {
    override def apply(values: Boolean = false): Future[Seq[SchemaHead]] = {
      val query: SqlStreamingAction[Vector[SchemaHead], SchemaHead, Effect] =
        sql"""SELECT A.schema_name, A.schema_latest_version
             FROM #$functionName($values) A;""".as[SchemaHead]
      schema.asInstanceOf[DatasetSchema].run(query)
    }
  }

  final class AddSchema(schema: DBSchema) extends DBValueFunction[SchemaInput, Long](schema) {
    override def apply(values: SchemaInput): Future[Long] = {
      val query =
        sql"""SELECT A.status, A.status_text, A.id_schema_version
             FROM #$functionName(${values.schemaName}, ${values.schemaVersion}, ${values.schemaDescription},
                ${values.fields}::JSONB, ${values.userName}
             ) A;""".as[(Int, String, Long)]
      for {
        records <- schema.asInstanceOf[DatasetSchema].run(query)
        status: Int = records.head._1
        statusText: String = records.head._2

        _ = if (status != 201) {
          throw DBFailException(status, statusText)
        }
        resultId = records.head._3
      } yield resultId
    }
  }

  final class GetSchema(schema: DBSchema) extends DBValueFunction[(String, Option[Int]), Schema](schema) {
    override def apply(values: (String, Option[Int])): Future[Schema] = {
      val query =
        sql"""SELECT A.*
             FROM #$functionName(${values._1}, ${values._2}) A;""".as[Schema]
      schema.asInstanceOf[DatasetSchema].run(query).map(_.head)
    }

    def apply(id: Long): Future[Schema] = {
      val query =
        sql"""SELECT A.*
             FROM #$functionName($id) A;""".as[Schema]
      schema.asInstanceOf[DatasetSchema].run(query).map(_.head)
    }
  }


}
