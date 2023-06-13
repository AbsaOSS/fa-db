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
import za.co.absa.fadb.slick.{SlickPgEngine, SlickPgFunction, SlickPgFunctionWithStatusSupport}
import za.co.absa.fadb.naming_conventions.SnakeCaseNaming.Implicits.namingConvention
import slick.jdbc.{GetResult, SQLActionBuilder}
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import scala.concurrent.Future
import DatasetSchema._
import za.co.absa.fadb.DBFunction.{DBSeqFunction, DBUniqueFunction}
import za.co.absa.fadb.statushandling.UserDefinedStatusHandling

/* The Schema doesn't need the dBEngine directly, but it seems cleaner this way to hand it over to schema's functions */
class DatasetSchema(implicit engine: SlickPgEngine) extends DBSchema {

  val addSchema = new AddSchema
  val getSchema = new GetSchema
  val list = new List
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

  case class SchemaHeader(entityName: String, entityLatestVersion: Int)

  final class AddSchema(implicit override val schema: DBSchema, override val dbEngine: SlickPgEngine)
    extends DBUniqueFunction[SchemaInput, Long, SlickPgEngine]
    with SlickPgFunctionWithStatusSupport[SchemaInput, Long]
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

  final class GetSchema(implicit override val schema: DBSchema, override val dbEngine: SlickPgEngine)
    extends DBUniqueFunction[(String, Option[Int]), Schema, SlickPgEngine]
    with SlickPgFunctionWithStatusSupport[(String, Option[Int]), Schema]
    with UserDefinedStatusHandling {

    /* This is an example of how to deal with overloaded DB functions - see different input type: Long vs what's in the class type: (String, Option[Int]) */
    def apply(id: Long): Future[Schema] = {
      val sql =
        sql"""SELECT A.*
             FROM #$functionName($id) A;"""

      val slickQuery = new dBEngine.QueryType(sql, slickConverter)
      dBEngine.unique[Schema](slickQuery)
    }

    override protected def sql(values: (String, Option[Int])): SQLActionBuilder = {
      sql"""SELECT A.*
            FROM #$functionName(${values._1}, ${values._2}) A;"""
    }

    override protected val slickConverter: GetResult[Schema] = GetResult{r =>
      Schema(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<)
    }

    override val OKStatuses: Set[Integer] = Set(200)
  }

  final class List(implicit override val schema: DBSchema, override val dbEngine: SlickPgEngine)
    extends DBSeqFunction[Boolean, SchemaHeader, SlickPgEngine]()
    with SlickPgFunction[Boolean, SchemaHeader] {

    override def apply(values: Boolean = false): Future[Seq[SchemaHeader]] = super.apply(values)

    override protected def sql(values: Boolean): SQLActionBuilder = {
      sql"""SELECT A.entity_name, A.entity_latest_version
            FROM #$functionName($values) as A;"""
    }

    override protected val slickConverter: GetResult[SchemaHeader] = GetResult(r => {SchemaHeader(r.<<, r.<<)})
  }
}
