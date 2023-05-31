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

/**
  * This trait serves the purpose of introducing functions that are common to all DB Function objects and mix-in traits
  * that offer certain implementations. This trait should help with the inheritance of all of these
  */
trait DBFunctionFabric {
  def functionName: String

  protected def fieldsToSelect: Seq[String] = Seq.empty
}
