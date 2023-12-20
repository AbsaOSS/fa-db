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

package za.co.absa.fadb

import doobie.implicits.javasql._
import doobie.util.meta.Meta

import java.sql.Timestamp
import java.time.ZoneId

// can't be named doobie due to a naming conflict
package object doobiedb {

  object postgres {

    object implicits {
      // Doobie does not provide a `Meta` instance for `ZonedDateTime` out of the box because database support for this type is not universal.
      // This `Meta` instance converts between `ZonedDateTime` and `Timestamp`, using the system's default time zone.
      // Please note that this might not be the correct behavior for your application if your database stores timestamps in a different time zone.
      // Meta[A] is a convenience type class for introducing a symmetric `Get`/`Put` pair into implicit scope, and for deriving new symmetric pairs.
      implicit val zonedDateTimeMeta: Meta[java.time.ZonedDateTime] = {
        Meta[Timestamp].timap(t => t.toLocalDateTime.atZone(ZoneId.systemDefault()))(zdt => Timestamp.from(zdt.toInstant))
      }
    }

  }

}
