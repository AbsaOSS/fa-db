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

package za.co.absa.faDB.examples

//import za.co.absa.dbViaProcedures.DBFunction.DBSetFunction
//import za.co.absa.dbViaProcedures.{DBSchema, DBSession}


case class Input(fieldA: String, fieldB: String)
case class Output(status: Integer, statusText: String)

//class Example(session: DBSession) extends DBSchema(session) {
//  val myFunction: DBSetFunction[String, Int] = new DBSetFunction[String, Int](this, Some("my_function")) {
//    override def apply(values: String): Seq[Int] = {
//      values.toSeq.map(_.toInt)
//    }
//  }
//
//
//  class MyFunction2 extends DBSetFunction[Input, Output](this) {
//    override def apply(values: Input): Seq[Output] = {
//      "SELECT status, status_text FROM example.my_function2(field_a := ?, field_b := ?)"
//      ???
//    }
//  }
//
//  val myFunction2 = new MyFunction2()
//
//}
