package za.co.absa.fadb.doobie

import doobie.util.{Get, Read}
import doobie.util.fragment.Fragment
import za.co.absa.fadb.Query

class DoobieQuery[R: Read](val fragment: Fragment)
                          (implicit val readR: Read[R]) // , val getR: Get[R]
  extends Query[R]
