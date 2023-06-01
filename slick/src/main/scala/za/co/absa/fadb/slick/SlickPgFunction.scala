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

package za.co.absa.fadb.slick

import slick.jdbc.{GetResult, PositionedResult, SQLActionBuilder}
import za.co.absa.fadb.{DBFunction, DBFunctionFabric, DBSchema, Query}

import scala.language.higherKinds

trait SlickPgFunction[T, R] extends DBFunctionFabric {

  val schema: DBSchema


  trait Foo[A] {} //Query

  type MyFunction[A] = Foo[A]

  class MyFunction2[A1] extends MyFunction[A1] //SlickQuery

  def bar[Q[String]  >: MyFunction[String] ]: Q[String]  = {
    new MyFunction2[String]
  }


  protected val alias = "A"

  val set: Set[String] = Set("a")

  protected def selectEntry: String = {
    val fieldsSeq = fieldsToSelect
    if (fieldsSeq.isEmpty) {
      "*"
    } else {
      val aliasToUse = if (alias.isEmpty) {
        ""
      } else {
        s"$alias."
      }
      fieldsToSelect.map(aliasToUse + _).mkString(",")
    }
  }

//  type MyQueryType[A]  = Query[A]
//  protected def query[Q[String] >: MyQueryType[String]](values: T): Q[String] = {
  protected def query[Q <: schema.dBEngine.QueryType[R]](values: T): Q = {
//     val q: Q = new SlickQuery(sql(values), slickConverter)
//    val q2: Q = ???
////    q2.foo
////    val q: (Q <: Query[R]) = new Query[R] {}
//    new Q{} //TODO
    ???
  }

//  protected def query(values: T): schema.dBEngine.QueryType[R] = {
//    new SlickQuery(sql(values), slickConverter)
//    ???
//    //new Query[R] {}
//  }


  protected def sql(values: T): SQLActionBuilder
  protected def slickConverter: GetResult[R]

}
