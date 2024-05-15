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
import za.co.absa.fadb.naming.NamingConvention
import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits.namingConvention

class DBSchemaUnitTests extends AnyFunSuite {

  test("schema name default") {
    class Foo extends DBSchema

    val schema = new Foo
    assert(schema.schemaName == "foo")
  }

  test("schema name overridden") {
    class Foo extends DBSchema("bar")

    val schema = new Foo
    assert(schema.schemaName == "bar")
  }

  test("schema name with naming convention without override") {
    object LowerCaseNamingConvention extends NamingConvention {
      def stringPerConvention(original: String): String = original.toLowerCase
    }
    class Bar extends DBSchema(LowerCaseNamingConvention, null)

    val schema = new Bar
    assert(schema.schemaName == "bar") // Assuming the naming convention converts "Bar" to "bar"
  }

  test("schema name with naming convention with override") {
    object LowerCaseNamingConvention extends NamingConvention {
      def stringPerConvention(original: String): String = original.toLowerCase
    }
    class Bar extends DBSchema(LowerCaseNamingConvention, "bar")

    val schema = new Bar
    assert(schema.schemaName == "bar")
  }

}
