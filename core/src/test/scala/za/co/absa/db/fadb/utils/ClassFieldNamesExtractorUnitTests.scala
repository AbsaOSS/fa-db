/*
 * Copyright 2025 ABSA Group Limited
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

package za.co.absa.db.fadb.utils

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.fadb.naming.LettersCase
import za.co.absa.db.fadb.naming.implementations.AsIsNaming
import za.co.absa.db.fadb.utils.ClassFieldNamesExtractorUnitTests._

class ClassFieldNamesExtractorUnitTests extends AnyFunSuiteLike {
  test("Extract from case class returns its fields") {
    val expected = Seq(
      "int_field",
      "string_field"
    )
    val fieldNames = ClassFieldNamesExtractor.extract[TestCaseClass]
    assert(fieldNames == expected)
  }

  test("Extract from class constructor fields returns its constructor fields, explicit naming convention") {
    val expected = Seq(
      "XFIELD",
      "YFIELD"
    )
    val fieldNames = ClassFieldNamesExtractor.extract[TestClass](new AsIsNaming(LettersCase.UpperCase))
    assert(fieldNames == expected)
  }

  test("Extract from class constructor fields returns its constructor fields, implicit naming convention") {
    implicit val namingConvention: AsIsNaming = new AsIsNaming(LettersCase.LowerCase)
    val expected = Seq(
      "xfield",
      "yfield"
    )
    val fieldNames = ClassFieldNamesExtractor.extract[TestClass]
    assert(fieldNames == expected)
  }

  test("Extract from trait fails") {
    intercept[IllegalArgumentException] {
      ClassFieldNamesExtractor.extract[TestTrait]
    }
  }

  test("Extract fails on simple type") {
    intercept[IllegalArgumentException] {
      ClassFieldNamesExtractor.extract[Boolean]
    }
  }

}

object ClassFieldNamesExtractorUnitTests {
  case class TestCaseClass(intField: Int, stringField: String) {
    def stringFunction: String = intField.toString + stringField
  }

  class TestClass(val xField: Int, val yField: String) {
    val w: String = xField.toString + yField

    def z: String = xField.toString + yField
  }

  trait TestTrait {
    def foo: String
  }

}
