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

package za.co.absa.fadb

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits.namingConvention

import scala.concurrent.{ExecutionContext, Future}

class DBSchemaSuite extends AnyFunSuite {

  private object EngineThrow extends DBEngine {
    override def run[R](query: QueryType[R]): Future[Seq[R]] = {
      throw new Exception("Should never get here")
    }

    override implicit val executor: ExecutionContext = ExecutionContext.Implicits.global
  }

  test("schema name default") {
    class Foo extends DBSchema(EngineThrow)

    val schema = new Foo
    assert(schema.schemaName == "foo")
  }

  test("schema name overridden") {
    class Foo extends DBSchema(EngineThrow, "bar")

    val schema = new Foo
    assert(schema.schemaName == "bar")
  }

}
