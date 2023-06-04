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

import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.DBFunctionFabric

trait SlickPgFunction[T, R] extends DBFunctionFabric {

  implicit val dbEngine: SlickPgEngine

  protected def sql(values: T): SQLActionBuilder
  protected def slickConverter: GetResult[R]

  protected val alias = "A"

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

  protected def query(values: T): dbEngine.QueryType[R] = {
    new SlickQuery(sql(values), slickConverter)
  }
}
