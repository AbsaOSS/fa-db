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

/**
  * Mix-in trait to use with [[za.co.absa.fadb.DBFunction DBFunction]] and Slick engine like [[SlickPgEngine]]. Implements the abstract function `query`
  * @tparam I - The input type of the function
  * @tparam R - The return type of the function
  */
trait SlickFunction[I, R] extends DBFunctionFabric {

  /**
    * A reference to the [[SlickPgEngine]] to use the [[za.co.absa.fadb.DBFunction DBFunction]] with
    */
  implicit val dbEngine: SlickPgEngine

  /**
    * This is expected to return SQL part of the [[SlickQuery]] (eventually returned by the `SlickPgFunction.query` function
    * @param values - the values to pass over to the database function
    * @return       - the Slick representation of the SQL
    */
  protected def sql(values: I): SQLActionBuilder

  /**
    * This is expected to return a method to convert the [[slick.jdbc.PositionedResult slick.PositionedResult]], the Slick general SQL result
    * format into the `R` type
    * @return - the converting function
    */
  protected def slickConverter: GetResult[R]

  /**
    * Alias to use within the SQL query
    */
  protected val alias = "FNC"

  /**
    * Helper function to use in the actual DB function class
    * @return the SELECT part of the function call SQL query
    */
  protected def selectEntry: String = { // TODO Not suggested to use until #6 will be implemented
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

  /**
    * This mix-in main reason of existence. It implements the `query` function for [[za.co.absa.fadb.DBFunction DBFunction]] for [[SlickPgEngine]]
    * @param values - the values to pass over to the database function
    * @return       - the SQL query in [[SlickQuery]] form
    */
  protected def query(values: I): dbEngine.QueryType[R] = {
    new SlickQuery(sql(values), slickConverter)
  }
}
