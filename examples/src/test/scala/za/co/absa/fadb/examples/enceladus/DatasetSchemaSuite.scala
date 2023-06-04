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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.fadb.examples.enceladus.DatasetSchema._
import slick.jdbc.PostgresProfile.api._
import za.co.absa.fadb.slick.SlickPgEngine
import za.co.absa.fadb.statushandling.StatusException

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DatasetSchemaSuite extends AnyWordSpec with Matchers {
  private val db = Database.forConfig("menasdb")
  private implicit val dbEngine: SlickPgEngine = new SlickPgEngine(db)
  private val schemas = new DatasetSchema

  private def checkException(exception: StatusException): Unit = {
    println(s"Requested failed with: ${exception.status.status} - ${exception.status.statusText}")
  }

  // test cases are set to be ignored now, as they are not idempotent and require other project's (Enceladus) data structures

  "listSchemas" should {
    "list the schemas" should {
      val ls = schemas.list()
      val result = Await.result(ls, Duration.Inf)
      result.foreach(println)
    }
  }

  "getSchema" should {
    "return the particular schema" when {
      "given name and version" ignore {
        val ls = schemas.getSchema(("aaa", Option(1)))
        val result = Await.result(ls, Duration.Inf)
        println(result)
      }
      "given id" ignore {
        val gs = schemas.getSchema(1000000000000051L)
        val result = Await.result(gs, Duration.Inf)
        println(result)
      }
    }
    "return the latest schema version" when {
      "only the schema name is given" ignore {
        val ls = schemas.getSchema(("aaa", None))
        val result = Await.result(ls, Duration.Inf)
        println(result)
      }
    }
    "fail" when {
      "schema does not exist" ignore {
        val exception = intercept[StatusException] {
          val gs = schemas.getSchema(("xxx", None))
          Await.result(gs, Duration.Inf)
        }
        checkException(exception)
      }
      "requested schema version does not exist" ignore {
        val exception = intercept[StatusException] {
          val gs = schemas.getSchema(("aaa", Some(1000)))
          Await.result(gs, Duration.Inf)
        }
        checkException(exception)
      }
    }
  }

  "addSchema" should {
    "add a schema" ignore  {
      val schemaInput = SchemaInput(
        schemaName = "bbe",
        schemaVersion = 1,
        schemaDescription = Option("Hello World"),
        fields = Option("""{"lorem": "ipsum"}"""),
        userName = "david"
      )
      val result = Await.result(schemas.addSchema(schemaInput), Duration.Inf)
      println(result)
    }
    "fail" when {
      "Schema already exists" ignore {
        val schemaInput = SchemaInput(
          schemaName = "aaa",
          schemaVersion = 2,
          schemaDescription = Option("Updates"),
          fields = Option("""{"foo": "bar"}"""),
          userName = "david"
        )
        val exception = intercept[StatusException] {
          Await.result(schemas.addSchema(schemaInput), Duration.Inf)
        }
        checkException(exception)
      }
      "Schema version wrong" ignore {
        val schemaInput = SchemaInput(
          schemaName = "aaa",
          schemaVersion = 1000,
          schemaDescription = Option("Will fail"),
          fields = Option("""{"not_getting_in": "1"}"""),
          userName = "david"
        )
        val exception = intercept[StatusException] {
          Await.result(schemas.addSchema(schemaInput), Duration.Inf)
        }
        checkException(exception)
      }
    }
  }
}
