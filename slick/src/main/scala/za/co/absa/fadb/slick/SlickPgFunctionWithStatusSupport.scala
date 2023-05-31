/*
 * Copyright 2023 ABSA Group Limited
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

import slick.jdbc.PositionedResult
import za.co.absa.fadb.DBFunction

import scala.util.Try

trait SlickPgFunctionWithStatusSupport[T, R] extends SlickPgFunction[T, R] {

  protected def checkStatus(status: Integer, statusText: String): Try[Unit]

  override protected def converter: PositionedResult => R = { queryResult =>
    val status: Int = queryResult.<<
    val statusText: String = queryResult.<<
    checkStatus(status, statusText).get //throw exception if status was off
    slickConverter(queryResult)
  }
}

object SlickPgFunctionWithStatusSupport {
  type DBSeqFunction[T, R] = DBFunction.DBSeqFunction[T, R, SlickQuery, PositionedResult] with SlickPgFunctionWithStatusSupport[T, R]
  type DBUniqueFunction[T, R] = DBFunction.DBUniqueFunction[T, R, SlickQuery, PositionedResult] with SlickPgFunctionWithStatusSupport[T, R]
  type DBOptionFunction[T, R] = DBFunction.DBOptionFunction[T, R, SlickQuery, PositionedResult] with SlickPgFunctionWithStatusSupport[T, R]
}
