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
package za.co.absa.ultet.model.table.alterations

import za.co.absa.ultet.model
import za.co.absa.ultet.model.ColumnName
import za.co.absa.ultet.model.table.{TableAlteration, TableName}

case class TableColumnNotNullDrop(tableName: TableName, columnName: ColumnName) extends TableAlteration {
  override def schemaName: model.SchemaName = ???

  override def sqlExpression: String = {
    s"""ALTER TABLE ${tableName.value}
       |ALTER COLUMN ${columnName.value} DROP NOT NULL;""".stripMargin
  }

  override def orderInTransaction: Int = 250
}