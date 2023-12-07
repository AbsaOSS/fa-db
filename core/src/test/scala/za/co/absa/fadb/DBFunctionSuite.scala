///*
// * Copyright 2022 ABSA Group Limited
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package za.co.absa.fadb
//
//import cats.implicits._
//import org.scalatest.funsuite.AnyFunSuite
//import za.co.absa.fadb.DBFunction.DBSingleResultFunction
//import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits.namingConvention
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//class DBFunctionSuite extends AnyFunSuite {
//
//  private def neverHappens: Nothing = {
//    throw new Exception("Should never get here")
//  }
//
//  class EngineThrow extends DBEngine[Future] {
//    override def run[R](query: QueryType[R]): Future[Seq[R]] = neverHappens
//  }
//
//  private object FooNamed extends DBSchema
//  private object FooNameless extends DBSchema("")
//
//  test("Function name check"){
//
//    class MyFunction(functionNameOverride: Option[String] = None)(implicit override val schema: DBSchema, val dbEngine: EngineThrow)
//      extends DBSingleResultFunction[Unit, Unit, EngineThrow, Future](None) {
//
//      override protected def query(values: Unit): dBEngine.QueryType[Unit] = neverHappens
//    }
//
//    val fnc1 = new MyFunction()(FooNamed, new EngineThrow)
//    val fnc2 = new MyFunction()(FooNameless, new EngineThrow)
//
//    assert(fnc1.functionName == "foo_named.my_function")
//    assert(fnc2.functionName == "my_function")
//  }
//
//  test("Function name override check"){
//    class MyFunction(implicit override val schema: DBSchema, val dbEngine: EngineThrow)
//      extends DBSingleResultFunction[Unit, Unit, EngineThrow, Future](Some("bar")) {
//
//      override protected def query(values: Unit): dBEngine.QueryType[Unit] = neverHappens
//    }
//
//    val fnc1 = new MyFunction()(FooNamed, new EngineThrow)
//    val fnc2 = new MyFunction()(FooNameless, new EngineThrow)
//
//    assert(fnc1.functionName == "foo_named.bar")
//    assert(fnc2.functionName == "bar")
//  }
//
//}
