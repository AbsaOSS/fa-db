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

package za.co.absa.db.fadb.status.aggregation

import za.co.absa.db.fadb.exceptions.StatusException
import za.co.absa.db.fadb.status.{FailedOrRows, FailedOrRow, Row}

/**
 *  `StatusAggregator` is a base trait that defines the interface for aggregating the error statuses of a function
 *  invocation. It provides methods to aggregate the error statuses into a single status information - this is
 *  typically needed for database functions that retrieve multiple records.
 */
trait StatusAggregator {

  /**
   *  Aggregates the error status information into a single error.
   *
   *  @param statusesWithData - The status of the function invocation with data.
   *  @return Either a `StatusException` if the status code indicates an error, or the data (along with the status
   *         information so that it's retrievable) if the status being returned doesn't indicate an error.
   */
  def aggregate[R](statusesWithData: Seq[FailedOrRow[R]]): FailedOrRows[R]
}

object StatusAggregator {
  private[aggregation] def gatherExceptions[R](eithersWithException: Seq[FailedOrRow[R]]): Seq[StatusException] = {
    eithersWithException.flatMap {
      case Left(exception) => Some(exception)
      case _               => None
    }
  }

  private[aggregation] def gatherDataWithStatuses[R](eithersWithData: Seq[FailedOrRow[R]]): Seq[Row[R]] = {
    eithersWithData.flatMap {
      case Left(_)                 => None
      case Right(dataWithStatuses) => Some(dataWithStatuses)
    }
  }
}
