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

import slick.jdbc.{GetResult, PositionedResult}
import za.co.absa.fadb.statushandling.FunctionStatus

import scala.util.Try

/**
  * An extension of the [[SlickPgFunction]] mix-in trait to add support of status handling
  * This trait expects another mix-in of [[za.co.absa.fadb.statushandling.StatusHandling]] (or implementation of `checkStatus` function)
  * @tparam T - The input type of the function
  * @tparam R - The return type of the function
  */
trait SlickPgFunctionWithStatusSupport[T, R] extends SlickPgFunction[T, R] {

  /**
    * Function which should actually check the status code returned by the DB function. Expected to got implemented by
    * [[za.co.absa.fadb.statushandling.StatusHandling]] successor trait. But of course can be implemented directly.
    * @param status - the status to check
    * @return       - Success or failure the status means
    */
  protected def checkStatus(status: FunctionStatus): Try[FunctionStatus]

  /**
    * A special extension of the converting function that first picks up status code and status check and checks for their
    * meaning. Then the original conversion is executed.
    * @param queryResult      - the result of the SQL query, the input of the original converting function
    * @param actualConverter  - the original converting function
    * @return                 - new converting function that also checks for status
    */
  private def converterWithStatus(queryResult: PositionedResult, actualConverter: GetResult[R]): R = {
    val status:Int = queryResult.<<
    val statusText: String = queryResult.<<
    checkStatus(FunctionStatus(status, statusText)).get //throw exception if status was off
    actualConverter(queryResult)
  }

  /**
    * Replaces the converter with one that also extracts and checks status code and status text.
    * @param values - the values to pass over to the database function
    * @return       - the SQL query in [[SlickQuery]] form
    */
  override protected def query(values: T): dbEngine.QueryType[R] = {
    val original = super.query(values)
    new SlickQuery[R](original.sql, GetResult{converterWithStatus(_, original.getResult)})
  }

}
