/*
 * Copyright 2021 ABSA Group Limited
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

package za.co.absa.faDB

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.faDB.DBFunction.QueryFunction
import za.co.absa.faDB.namingConventions.SnakeCaseNaming.Implicits.namingConvention

import scala.concurrent.Future

class DBSchemaSuite extends AnyFunSuite {
  private val executor = new DBExecutor[String] {
    override def run[R](fnc: QueryFunction[String, R]): Future[Seq[R]] = ???
  }

  test("schema name default") {

    class Foo(executor: DBExecutor[String]) extends DBSchema(executor)
    val schema = new Foo(executor)
    assert(schema.schemaName == "foo")
  }
  test("schema name overridden") {
    class Foo(executor: DBExecutor[String]) extends DBSchema(executor, Some("bar"))

    val schema = new Foo(executor)
    assert(schema.schemaName == "bar")
  }
}
