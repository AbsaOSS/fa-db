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

package za.co.absa.fadb.doobie

import za.co.absa.fadb.status.FunctionStatus

object Results {

  /**
   *  `SuccessfulResult` is a case class that represents a successful result of a function.
   *
   *  @param functionStatus the status of the function
   *  @param result the result of the function
   */
  case class SuccessfulResult[R](functionStatus: FunctionStatus, result: R)

  /**
   *  `FailedResult` is a case class that represents a failed result of a function.
   *
   *  @param functionStatus the status of the function
   *  @param failure the exception that caused the function to fail
   */
  case class FailedResult(functionStatus: FunctionStatus, failure: Throwable)

  /**
   *  `ResultWithStatus` is a type alias for `Either[FailedResult, SuccessfulResult[R]]`.
   *  It represents a result of a function that can either be successful or failed.
   */
  type ResultWithStatus[R] = Either[FailedResult, SuccessfulResult[R]]
}
