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

import doobie.util.meta.Meta
import org.scalatest.funsuite.AnyFunSuite

class AvailableMetaInstancesUnitTests extends AnyFunSuite {

  test("Meta[java.time.OffsetDateTime] available from doobie.implicits.javatimedrivernative") {
    import doobie.implicits.javatimedrivernative.JavaOffsetDateTimeMeta
    Meta[java.time.OffsetDateTime]
  }

  test("Meta[java.time.OffsetDateTime] available from doobie.postgres.implicits") {
    import doobie.postgres.implicits._
    Meta[java.time.OffsetDateTime]
  }

  test("Meta[java.time.Instant] from doobie.postgres.implicits") {
    import doobie.postgres.implicits._
    Meta[java.time.Instant]
  }

  test("Meta[java.time.LocalDateTime] from doobie.postgres.implicits") {
    import doobie.postgres.implicits._
    Meta[java.time.LocalDateTime]
  }

  test("Meta[java.time.LocalDate] from doobie.postgres.implicits") {
    import doobie.postgres.implicits._
    Meta[java.time.LocalDate]
  }

  test("Meta[java.time.LocalTime] from doobie.implicits.javatimedrivernative") {
    import doobie.implicits.javatimedrivernative.JavaLocalTimeMeta
    Meta[java.time.LocalTime]
  }

  test("Meta[java.time.LocalTime] from doobie.postgres.implicits") {
    import doobie.postgres.implicits._
    Meta[java.time.LocalTime]
  }

  test("Meta[java.util.Date] out of the box") {
    Meta[java.util.Date]
  }

}
