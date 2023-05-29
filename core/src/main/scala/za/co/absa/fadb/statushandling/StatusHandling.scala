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

package za.co.absa.fadb.statushandling

import za.co.absa.fadb.DBFunctionFabric
import za.co.absa.fadb.statushandling.StatusHandling.{defaultStatusFieldName, defaultStatusTextFieldName}

import scala.util.Try

/**
  * A basis for mix-in traits for [[DBFunction]] that support `status` and `status_text` for easier handling
  */
trait StatusHandling extends DBFunctionFabric{

  def statusFieldName: String = defaultStatusFieldName
  def statusTextFieldName: String = defaultStatusTextFieldName

  override protected def fieldsToSelect: Seq[String] = {
    Seq(statusFieldName, statusTextFieldName) ++ super.fieldsToSelect
  }

  protected def checkStatus(status: Integer, statusTex: String): Try[Unit]
}

object StatusHandling {
  val defaultStatusFieldName = "status"
  val defaultStatusTextFieldName = "status_test"
}
