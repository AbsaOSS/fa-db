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

class DBSchemaSuite extends AnyFunSuite {


  test("schema name default") {
    val session = new DBSession("")

    class Foo(session: DBSession) extends DBSchema(session)
    val schema = new Foo(session)
    assert(schema.schemaName == "foo")
  }
  test("schema name overriden") {
    val session = new DBSession("")
    class Foo(session: DBSession) extends DBSchema(session, Some("bar"))

    val schema = new Foo(session)
    assert(schema.schemaName == "bar")
  }
}
