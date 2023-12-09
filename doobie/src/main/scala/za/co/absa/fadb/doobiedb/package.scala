package za.co.absa.fadb

import doobie.util.meta.Meta
import doobie.implicits.javasql._

import java.sql.Timestamp
import java.time.ZoneId

// can't be named doobie due to a naming conflict
package object doobiedb {

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
