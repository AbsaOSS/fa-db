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

package za.co.absa.db.fadb.doobie

import doobie.Read

/**
 * Represents a function status with data (basically a row returned from a DB).
 */
case class StatusWithData[R](status: Int, statusText: String, data: Option[R])

object StatusWithData {

  implicit def read[T](implicit r: Read[Option[T]]): Read[StatusWithData[T]] =
    Read[(Int, String, Option[T])].map {
      case (status, statusText, data) => StatusWithData(status, statusText, data)
    }

  // This is for backward compatibility of #133 (which makes the data optional by default when reading from DB),
  // as some users were overcoming the bug by wrapping the result type in Option themselves.
  // Doobie provides Read for Option[T] for most of the types out of the box, except when T itself is an Option.
  implicit def readOptionToReadDoubleOption[T](
                                                implicit r: Read[StatusWithData[T]]
                                              ): Read[StatusWithData[Option[T]]] = {
    r.map {
      case statusWithData @ StatusWithData(_, _, Some(data)) => statusWithData.copy(data = Some(Some(data)))
      case statusWithData @ StatusWithData(_, _, None) => statusWithData.copy(data = None)
    }
  }

}
