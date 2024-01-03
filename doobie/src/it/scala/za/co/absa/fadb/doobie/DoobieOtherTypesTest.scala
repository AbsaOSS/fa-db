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

package za.co.absa.fadb.doobie

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.{DoobieSingleResultFunction, DoobieSingleResultFunctionWithStatus}
import za.co.absa.fadb.exceptions.DataConflictException
import za.co.absa.fadb.status.FunctionStatus
import za.co.absa.fadb.status.handling.implementations.StandardStatusHandling

import java.net.InetAddress
import java.util.UUID


class DoobieOtherTypesTest extends AnyFunSuite with DoobieTest {

  import doobie.postgres.implicits._

  case class OtherTypesData(
                             id: Int,
                             ltreeCol: String,
                             inetCol: InetAddress,
                             macaddrCol: String,
                             hstoreCol: Map[String, String],
                             cidrCol: String,
                             jsonCol: String,
                             jsonbCol: String,
                             uuidCol: UUID,
                             arrayCol: Array[Int]
                           )


  class ReadOtherTypes(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieSingleResultFunction[Int, OtherTypesData, IO] {

    override def sql(values: Int)(implicit read: Read[OtherTypesData]): Fragment =
      sql"SELECT * FROM ${Fragment.const(functionName)}($values)"
  }

  class InsertOtherTypes(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieSingleResultFunctionWithStatus[OtherTypesData, Option[Int], IO] with StandardStatusHandling {

    override def sql(values: OtherTypesData)(implicit read: Read[StatusWithData[Option[Int]]]): Fragment =
      sql"""
           SELECT * FROM ${Fragment.const(functionName)}(
             ${values.id},
             ${values.ltreeCol}::ltree,
             ${values.inetCol}::inet,
             ${values.macaddrCol}::macaddr,
             ${values.hstoreCol}::hstore,
             ${values.cidrCol}::cidr,
             ${values.jsonCol}::json,
             ${values.jsonbCol}::jsonb,
             ${values.uuidCol}::uuid,
             ${values.arrayCol}::integer[]
           )
         """
  }

  private val readOtherTypes = new ReadOtherTypes()(Runs, new DoobieEngine(transactor))
  private val insertOtherTypes = new InsertOtherTypes()(Runs, new DoobieEngine(transactor))

  test("Reading other common data types from database") {
    val expectedData = OtherTypesData(
      id = 1,
      ltreeCol = "Top.Science.Astronomy",
      inetCol = InetAddress.getByName("192.168.1.1"),
      macaddrCol = "08:00:2b:01:02:03",
      hstoreCol = Map("key" -> "value"),
      cidrCol = "192.168.1.0/24",
      jsonCol = """{"key": "value"}""",
      jsonbCol = """{"key": "value"}""",
      uuidCol = UUID.fromString("b574cb0f-4790-4798-9b3f-824c7fab69dc"),
      arrayCol = Array(1, 2, 3)
    )

    val result = readOtherTypes(1).unsafeRunSync()

    assert(result.id == expectedData.id)
    assert(result.ltreeCol == expectedData.ltreeCol)
    assert(result.inetCol == expectedData.inetCol)
    assert(result.macaddrCol == expectedData.macaddrCol)
    assert(result.hstoreCol == expectedData.hstoreCol)
    assert(result.cidrCol == expectedData.cidrCol)
    assert(result.jsonCol == expectedData.jsonCol)
    assert(result.jsonbCol == expectedData.jsonbCol)
    assert(result.uuidCol == expectedData.uuidCol)
    assert(result.arrayCol sameElements expectedData.arrayCol)
  }

  test("Writing other common data types to database") {
    val data = OtherTypesData(
      id = 3,
      ltreeCol = "Top.Science.Astronomy",
      inetCol = InetAddress.getByName("192.168.1.1"),
      macaddrCol = "08:00:2b:01:02:03",
      hstoreCol = Map("key" -> "value"),
      cidrCol = "192.168.1/24",
      jsonCol = """{"key": "value"}""",
      jsonbCol = """{"key": "value"}""",
      uuidCol = UUID.randomUUID(),
      arrayCol = Array(1, 2, 3)
    )
    val result = insertOtherTypes(data).unsafeRunSync()
    assert(result == Left(DataConflictException(FunctionStatus(31, "data conflict"))))
  }

}
