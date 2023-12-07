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

import cats.implicits._
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.DBFunction.{DBMultipleResultFunction, DBOptionalResultFunction, DBSingleResultFunction}
import za.co.absa.fadb.{DBFunctionWithStatus, DBSchema}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private[slick] trait SlickFunctionBase[I, R] {

  /**
   *  The `GetResult[R]` instance used to read the query result into `R`.
   */
  protected def slickConverter: GetResult[R]

  /**
   *  Generates a Slick `SQLActionBuilder` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the Slick `SQLActionBuilder` representing the SQL query
   */
  protected def sql(values: I): SQLActionBuilder

  def fieldsToSelect: Seq[String]

  /**
   * Alias to use within the SQL query
   */
  protected val alias = "FNC"

  /**
   * Helper function to use in the actual DB function class
   *
   * @return the SELECT part of the function call SQL query
   */
  protected def selectEntry: String = {
    val fieldsSeq = fieldsToSelect
    if (fieldsSeq.isEmpty) {
      "*"
    } else {
      val aliasToUse = if (alias.isEmpty) {
        ""
      } else {
        s"$alias."
      }
      fieldsToSelect.map(aliasToUse + _).mkString(",")
    }
  }
}

private[slick] trait SlickFunction[I, R] extends SlickFunctionBase[I, R] {

  /**
   *  Generates a `SlickQuery[R]` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the `SlickQuery[R]` representing the SQL query
   */
  protected def query(values: I): SlickQuery[R] = new SlickQuery(sql(values), slickConverter)
}

private[slick] trait SlickFunctionWithStatus[I, R] extends SlickFunctionBase[I, R] {

  /**
   *  Generates a `SlickQueryWithStatus[R]` representing the SQL query for the function with status support.
   *
   *  @param status - the status to check
   *  @return       - Success or failure the status means
   */
  protected def query(values: I): SlickQueryWithStatus[R] = new SlickQueryWithStatus[R](sql(values), slickConverter)
}

object SlickFunction {

  abstract class SlickSingleResultFunctionWithStatus[I, R](functionNameOverride: Option[String] = None)(
    implicit override val schema: DBSchema,
    DBEngine: SlickPgEngine
  ) extends DBFunctionWithStatus[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunctionWithStatus[I, R]

  abstract class SlickSingleResultFunction[I, R](functionNameOverride: Option[String] = None)(
    implicit override val schema: DBSchema,
    DBEngine: SlickPgEngine
  ) extends DBSingleResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R]

  abstract class SlickMultipleResultFunction[I, R](functionNameOverride: Option[String] = None)(
    implicit override val schema: DBSchema,
    DBEngine: SlickPgEngine
  ) extends DBMultipleResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R]

  abstract class SlickOptionalResultFunction[I, R](functionNameOverride: Option[String] = None)(
    implicit override val schema: DBSchema,
    DBEngine: SlickPgEngine
  ) extends DBOptionalResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R]
}
