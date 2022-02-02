/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.faDB

import za.co.absa.faDB.namingConventions.NamingConvention

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

abstract class DBSchema2[E](val executor: Executor[E], schemaNameOverride: Option[String] = None)
                           (implicit namingConvention: NamingConvention) {


  def objectNameFromClassName(c: Class[_]): String = {
    namingConvention.fromClassNamePerConvention(c)
  }

  val schemaName: String = schemaNameOverride.getOrElse(objectNameFromClassName(getClass))

  def execute[R](query: E => Future[Seq[R]]): Future[Seq[R]] = {
    executor.run(query)
  }

  def unique[R](query: E => Future[Seq[R]]): Future[R] = {
    for {
      all <- execute(query)
    } yield all.head
  }

  def option[R](query: E => Future[Seq[R]]): Future[Option[R]] = {
    for {
      all <- execute(query)
    } yield all.headOption
  }
}
