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

import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.{SlickPgEngine, SlickPgFunction, SlickPgFunctionWithStatusSupport, SlickQuery}
import za.co.absa.fadb.naming_conventions.SnakeCaseNaming.Implicits.namingConvention
import slick.jdbc.{GetResult, SQLActionBuilder}
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import scala.concurrent.Future
import DatasetSchema._
import za.co.absa.fadb.DBFunction.{DBSeqFunction, DBUniqueFunction}
import za.co.absa.fadb.statushandling.{StatusException, UserDefinedStatusHandling}

class DatasetSchema(engine: SlickPgEngine) extends DBSchema(engine) {



  private implicit val schema: DBSchema = this
  val addSchema = new AddSchema
  val getSchema = new GetSchema
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

  final class AddSchema(implicit schema: DBSchema)
    extends DBUniqueFunction[SchemaInput, Long](schema)
    with SlickPgFunction[SchemaInput, Long]
    with UserDefinedStatusHandling {

    override protected def sql(values: SchemaInput): SQLActionBuilder = {
      sql"""SELECT A.status, A.status_text, A.id_schema_version
            FROM #$functionName(${values.schemaName}, ${values.schemaVersion}, ${values.schemaDescription},
              ${values.fields}::JSONB, ${values.userName}
            ) A;"""
    }

    override protected def slickConverter: GetResult[Long] = GetResult.GetLong

    override def OKStatuses: Set[Integer] = Set(201)

  }

  final class GetSchema(implicit schema: DBSchema)
    extends DBUniqueFunction[(String, Option[Int]), Schema](schema)
    with SlickPgFunctionWithStatusSupport[(String, Option[Int]), Schema]
    with UserDefinedStatusHandling {

//  TODO +++
//    def apply(id: Long): Future[Schema] = {
//      val sql =
//        sql"""SELECT A.*
//             FROM #$functionName($id) A;"""
//
//      val slickQuery: schema.QueryType[Schema] = SlickQuery(sql, slickConverter)
//      schema.dBEngine.unique[Schema](slickQuery)
//    }

    override protected def sql(values: (String, Option[Int])): SQLActionBuilder = {
      sql"""SELECT A.*
            FROM #$functionName(${values._1}, ${values._2}) A;"""
    }

    override protected val slickConverter: GetResult[Schema] = GetResult{r =>
      Schema(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<)
    }

    override val OKStatuses: Set[Integer] = Set(200)
  }

  final class ListSchemas(implicit schema: DBSchema)
    extends DBSeqFunction[Boolean, SchemaHeader](schema)
    with SlickPgFunction[Boolean, SchemaHeader] {

    override def apply(values: Boolean = false): Future[Seq[SchemaHeader]] = super.apply(values)

    override protected def sql(values: Boolean): SQLActionBuilder = {
      sql"""SELECT A.schema_name, A.schema_latest_version
            FROM #$functionName($values) as A;"""
    }

    override protected val slickConverter: GetResult[SchemaHeader] = GetResult(r => {SchemaHeader(r.<<, r.<<)})
  }
}
