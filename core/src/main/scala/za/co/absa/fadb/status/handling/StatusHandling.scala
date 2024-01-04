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

package za.co.absa.fadb.status.handling

import za.co.absa.fadb.FunctionStatusWithData
import za.co.absa.fadb.exceptions.StatusException

/**
 *  [[StatusHandling]] is a base trait that defines the interface for handling the status of a function invocation.
 *  It provides a method to check the status of a function invocation with data.
 */
trait StatusHandling {

  /**
   *  Checks the status of a function invocation.
   *  @param statusWithData - The status of the function invocation with data.
   *  @return Either a [[za.co.absa.fadb.exceptions.StatusException]] if the status code indicates an error, or the data if the status code is successful.
   */
  def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A]
}
