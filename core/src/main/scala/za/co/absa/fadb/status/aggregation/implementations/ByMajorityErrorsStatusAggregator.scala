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
  *  `ByMajorityErrorsStatusAggregator` is a trait that extends the `StatusAggregator` interface.
  *  It provides an implementation for aggregating error statuses of a function invocation into a single error
  *  by choosing the error that occurred the most.
  */
trait ByMajorityErrorsStatusAggregator extends StatusAggregator {

  private[aggregation] def gimmeMajorityWinner[T](inputData: Seq[T]): Option[T] = {
    if (inputData.isEmpty) {
      None
    } else {
      val grouped = inputData.groupBy(identity)
      val maxCount = grouped.values.map(_.size).max

      // list of most frequent one (or ones if maxCount is the same for multiple records)
      val mostOccurredAll = grouped.filter(_._2.size == maxCount).keys.toList

      // find stops on first occurrence - so it returns the first it found from `mostOccurredAll` - in case that there
      // are more 'majorityWinners', only the first one from `inputData` will be returned
      val mostOccurredFirst = inputData.find(mostOccurredAll.contains(_))

      mostOccurredFirst
    }
  }

  override def aggregate[R](statusesWithData: Seq[ExceptionOrStatusWithDataRow[R]]): ExceptionOrStatusWithDataResultAgg[R] = {
    val allErrors = gatherExceptions(statusesWithData)
    val majorityError = gimmeMajorityWinner(allErrors)

    majorityError match {
      case Some(statusException)  => Left(statusException)
      case None                   => Right(gatherDataWithStatuses(statusesWithData))
    }
  }

}
