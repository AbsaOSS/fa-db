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
import za.co.absa.fadb.naming_conventions.NamingConvention
import za.co.absa.fadb.statushandling.StatusHandling.{defaultStatusField, defaultStatusTextField}

import scala.util.Try

/**
  * A basis for mix-in traits for [[za.co.absa.fadb.DBFunction DBFunction]] that support `status` and `status text` for easier handling
  */
trait StatusHandling extends DBFunctionFabric {

  /**
    * @return - the naming convention to use when converting the internal status and status text fields to DB fields
    */
  def namingConvention: NamingConvention

  /**
    * Verifies if the give status means success or failure
    * @param status - the status to check
    * @return       - Success or failure the status means
    */
  protected def checkStatus(status: FunctionStatus): Try[FunctionStatus]
  protected def checkStatus(status: Integer, statusText: String): Try[FunctionStatus] = checkStatus((FunctionStatus(status, statusText)))

  def statusField: String = defaultStatusField
  def statusTextField: String = defaultStatusTextField

  /**
    * A mix-in to add the status fields into the SELECT statement
    * @return a sequence of fields to use in SELECT
    */
  override protected def fieldsToSelect: Seq[String] = {
    Seq(
      namingConvention.stringPerConvention(statusField),
      namingConvention.stringPerConvention(statusTextField)
    ) ++ super.fieldsToSelect
  }

}

object StatusHandling {
  val defaultStatusField = "status"
  val defaultStatusTextField = "statusText"
}
