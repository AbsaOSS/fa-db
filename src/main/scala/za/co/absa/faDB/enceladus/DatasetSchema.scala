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
import za.co.absa.faDB.enceladus.DatasetSchema.{AddSchema, GetSchema, ListSchemas}
import za.co.absa.faDB.{DBSchema, DBSchema2, DBSession}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlStreamingAction
import za.co.absa.faDB.Slick.{SlickExecutor, SlickQuery, SlickSession}
import za.co.absa.faDB.exceptions.DBFailException

import java.sql.Timestamp
import za.co.absa.faDB.namingConventions.SnakeCaseNaming.Implicits.namingConvention


class DatasetSchema(session: SlickSession) extends DBSchema(session) {

  private implicit val schema: DatasetSchema = this
  val addSchema = new AddSchema
  val getSchema = new GetSchema(this)
  val listSchemas = new ListSchemas
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

  final class AddSchema(implicit schema: DatasetSchema) extends DBUniqueFunction[SchemaInput, Long](schema) {
    override def apply(values: SchemaInput): Future[Long] = {
      val query: SqlStreamingAction[Vector[(Int, String, Long)], (Int, String, Long), Effect] =
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

  final class GetSchema(implicit schema: DatasetSchema) extends DBUniqueFunction[(String, Option[Int]), Schema](schema) {
    override def apply(values: (String, Option[Int])): Future[Schema] = {
      val query: SqlStreamingAction[Vector[Schema], Schema, Effect] =
        sql"""SELECT A.*
             FROM #$functionName(${values._1}, ${values._2}) A;""".as[Schema]
      //schema.asInstanceOf[DatasetSchema].run(query).map(_.head)
      val res = schema.session.asInstanceOf[SlickSession].db.run(query)
      res.map(_.head)
    }

    def hmmm[R](query: SqlStreamingAction[Vector[R], R, Effect]): Future[Seq[R]] = {
      schema.session.asInstanceOf[SlickSession].db.run(query)
    }

    def apply(id: Long): Future[Schema] = {
      val query =
        sql"""SELECT A.*
             FROM #$functionName($id) A;""".as[Schema]
      schema.asInstanceOf[DatasetSchema].run(query).map(_.head)
    }
  }


}



