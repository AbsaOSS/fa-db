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

package za.co.absa.fadb.slick.support

import com.github.tminglei.slickpg.{ExPostgresProfile, utils}
import com.github.tminglei.slickpg.utils.PgCommonJdbcTypes
import slick.jdbc.{GetResult, JdbcType, PositionedParameters, PositionedResult, PostgresProfile, SetParameter}

import java.sql.JDBCType
import java.util.UUID
import scala.reflect.classTag

trait PgUUIDSupport extends PgCommonJdbcTypes { driver: PostgresProfile =>
  import driver.api._

  trait UUIDCodeGenSupport {
    // register types to let `ExModelBuilder` find them
    driver match {
      case profile: ExPostgresProfile => profile.bindPgTypeToScala("uuid", classTag[UUID])
      case _ =>
    }
  }

  trait UUIDPlainImplicits extends UUIDCodeGenSupport{
    import utils.PlainSQLUtils._

    implicit class PgPositionedResult(val r: PositionedResult) {
      def nextUUID: UUID = r.nextObject().asInstanceOf[UUID]

      def nextUUIDOption: Option[UUID] = r.nextObjectOption().map(_.asInstanceOf[UUID])
    }

    implicit object SetUUID extends SetParameter[UUID] { def apply(v: UUID, pp: PositionedParameters): Unit = { pp.setObject(v, JDBCType.BINARY.getVendorTypeNumber) } }

    implicit val getUUID: GetResult[UUID] = mkGetResult(_.nextUUID)
    implicit val getUUIDOption: GetResult[Option[UUID]] = mkGetResult(_.nextUUIDOption)
    implicit val setUUID: SetParameter[UUID] = new SetParameter[UUID] { def apply(v: UUID, pp: PositionedParameters): Unit = { pp.setObject(v, JDBCType.BINARY.getVendorTypeNumber) } }
    implicit val setUUIDOption: SetParameter[Option[UUID]] =  new SetParameter[Option[UUID]] { def apply(v: Option[UUID], pp: PositionedParameters): Unit = { pp.setObject(v.orNull, JDBCType.BINARY.getVendorTypeNumber) } }

  }
}
