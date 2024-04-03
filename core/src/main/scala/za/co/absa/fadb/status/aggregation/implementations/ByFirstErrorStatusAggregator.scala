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

import za.co.absa.fadb.status.aggregation.StatusAggregator
import za.co.absa.fadb.status.{ExceptionOrStatusWithDataResultAgg, ExceptionOrStatusWithDataRow}

/**
  *  `ByFirstErrorStatusAggregator` is a trait that extends the `StatusAggregator` interface.
  *  It provides an implementation for aggregating error statuses of a function invocation into a single error
  *  by choosing the first error encountered to be the representative one (i.e. if there are multiple errors of other
  *  types being returned, only the first one would be chosen and the rest would be ignored).
  */
trait ByFirstErrorStatusAggregator extends StatusAggregator {

  override def aggregate[R](statusesWithData: Seq[ExceptionOrStatusWithDataRow[R]]): ExceptionOrStatusWithDataResultAgg[R] = {
    val firstError = gatherExceptions(statusesWithData).headOption

    val dataFinal = gatherDataWithStatuses(statusesWithData)

    firstError match {
      case Some(statusException) => Left(statusException)
      case None => Right(dataFinal)
    }
  }

}
