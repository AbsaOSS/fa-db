package za.co.absa.fadb.doobie

import doobie.util.Read
import doobie.util.fragment.Fragment
import za.co.absa.fadb.Query

/**
 *  `DoobieQuery` is a class that extends `Query` with `R` as the result type.
 *  It uses Doobie's `Fragment` to represent SQL queries.
 *
 *  @param fragment the Doobie fragment representing the SQL query
 *  @param readR the `Read[R]` instance used to read the query result into `R`
 */
class DoobieQuery[R: Read](val fragment: Fragment)(implicit val readR: Read[R]) extends Query[R]
