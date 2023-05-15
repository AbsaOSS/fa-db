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

package za.co.absa.fadb

import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Future
import za.co.absa.fadb.naming_conventions.SnakeCaseNaming.Implicits.namingConvention

class DBFunctionSuite extends AnyFunSuite {
  private type Engine = String // just an engine type, not relevant Here

  private object ExecutorThrow extends DBExecutor[String] {
    override def run[R](fnc: QueryFunction[Engine, R]): Future[Seq[R]] = {
      throw new Exception("Should never get here")
    }
  }

  private object FooNamed extends DBSchema(ExecutorThrow)
  private object FooNameless extends DBSchema(ExecutorThrow, Some(""))

  test("Function name check"){
    case class MyFunction(schema: DBSchema[Engine]) extends DBFunction(schema) {
      override protected def queryFunction(values: Nothing): QueryFunction[Engine, Nothing] = {
        throw new Exception("Should never get here")
      }
    }

    val fnc1 = MyFunction(FooNamed)
    val fnc2 = MyFunction(FooNameless)

    assert(fnc1.functionName == "foo_named.my_function")
    assert(fnc2.functionName == "my_function")
  }

  test("Function name override check"){
    case class MyFunction(schema: DBSchema[Engine]) extends DBFunction(schema, Some("bar")) {
      override protected def queryFunction(values: Nothing): QueryFunction[Engine, Nothing] = {
        throw new Exception("Should never get here")
      }
    }

    val fnc1 = MyFunction(FooNamed)
    val fnc2 = MyFunction(FooNameless)

    assert(fnc1.functionName == "foo_named.bar")
    assert(fnc2.functionName == "bar")
  }

}
