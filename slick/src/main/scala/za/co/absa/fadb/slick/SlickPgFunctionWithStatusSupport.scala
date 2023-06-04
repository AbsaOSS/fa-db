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

trait SlickPgFunctionWithStatusSupport[T, R] extends SlickPgFunction[T, R] {

  protected def checkStatus(status: FunctionStatus): Try[FunctionStatus]

  private def converterWithStatus(queryResult: PositionedResult, actualConverter: GetResult[R]): R = {
    val status:Int = queryResult.<<
    val statusText: String = queryResult.<<
    checkStatus(FunctionStatus(status, statusText)).get //throw exception if status was off
    actualConverter(queryResult)
  }

  override protected def query(values: T): dbEngine.QueryType[R] = {
    val original = super.query(values)
    new SlickQuery[R](original.sql, GetResult{converterWithStatus(_, original.getResult)})
  }

}
