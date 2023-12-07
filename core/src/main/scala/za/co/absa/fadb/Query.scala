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

import za.co.absa.fadb.status.StatusException
import za.co.absa.fadb.status.handling.QueryStatusHandling

/**
  * The basis for all query types of [[DBEngine]] implementations
 *
 * @tparam R - the return type of the query
  */
trait Query[R]


trait QueryWithStatus[A, B, R] extends QueryStatusHandling {
  def processStatus(initialResult: A): FunctionStatusWithData[B]
  def toStatusExceptionOrData(statusWithData: FunctionStatusWithData[B]): Either[StatusException, R]
  def getResultOrException(initialResult: A): Either[StatusException, R] = toStatusExceptionOrData(processStatus(initialResult))
//  def getResultOrException(initialResult: Vector[A]): Either[StatusException, R] = initialResult.map(getResultOrException).head
}
