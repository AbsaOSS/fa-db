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

package za.co.absa.fadb.status.aggregation.implementations

import za.co.absa.fadb.status.aggregation.StatusAggregation
import za.co.absa.fadb.status.{ExceptionOrStatusWithDataResultAgg, ExceptionOrStatusWithDataRow}

/**
  *  `AggregateByFirstRow` is a trait that extends the `StatusAggregation` interface.
  *  It provides an implementation for aggregating error statuses of a function invocation into a single error
  *  by choosing the first row that was returned to be the representative one
  *  (i.e. if there is an error on row two or later, it would be ignored).
  */
trait AggregateByFirstRow extends StatusAggregation {

  override def aggregate[R](statusesWithData: Seq[ExceptionOrStatusWithDataRow[R]]): ExceptionOrStatusWithDataResultAgg[R] = {
    val firstRow = statusesWithData.headOption

    val dataFinal = gatherDataWithStatuses(statusesWithData)

    firstRow match {
      case Some(exceptionOrDataWithStatuses) => exceptionOrDataWithStatuses match {
        case Left(statusException) => Left(statusException)
        case Right(_) => Right(dataFinal)
      }
      case None => Right(Seq.empty)
    }
  }

}
