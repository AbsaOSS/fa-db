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

//
//class DBFunctionSuite extends AnyFunSuite {
//  private object EngineThrow extends DBEngine {
//    override def run[R](query: QueryType[R]): Future[Seq[R]] = {
//      throw new Exception("Should never get here")
//    }
//  }
//
//  private object FooNamed extends DBSchema(EngineThrow)
//  private object FooNameless extends DBSchema(EngineThrow, Some(""))
//
//  test("Function name check"){
//    case class MyFunction(override val schema: DBSchema) extends DBFunction(schema) {
//      //override protected def query(values: Nothing): schema.dBEngine.QueryType[Nothing] = throw new Exception("Should never get here")
//
//      override protected def query[Q >: schema.dBEngine.QueryType[Nothing]](values: Nothing): Q = ??? //TODO
//    }
//
//    val fnc1 = MyFunction(FooNamed)
//    val fnc2 = MyFunction(FooNameless)
//
//    assert(fnc1.functionName == "foo_named.my_function")
//    assert(fnc2.functionName == "my_function")
//  }
//
//  test("Function name override check"){
//    case class MyFunction(override val schema: DBSchema) extends DBFunction(schema, Some("bar")) {
////      override protected def query(values: Nothing): schema.dBEngine.QueryType[Nothing] = throw new Exception("Should never get here")
//      override protected def query[Q >: schema.dBEngine.QueryType[Nothing]](values: Nothing): Q = ??? //TODO
//    }
//
//    val fnc1 = MyFunction(FooNamed)
//    val fnc2 = MyFunction(FooNameless)
//
//    assert(fnc1.functionName == "foo_named.bar")
//    assert(fnc2.functionName == "bar")
//  }
//
//}
