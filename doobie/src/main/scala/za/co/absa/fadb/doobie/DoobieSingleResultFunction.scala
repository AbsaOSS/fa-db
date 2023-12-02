package za.co.absa.fadb.doobie

import cats.effect.IO
import doobie.util.{Get, Read}
import doobie.util.fragment.Fragment
import za.co.absa.fadb.DBFunction.DBSingleResultFunction
import za.co.absa.fadb.DBSchema


trait DoobieFunction[I, R] {
  implicit def readR: Read[R]
  implicit def getR: Get[R]
  def sql(values: I)(implicit read: Read[R], get: Get[R]): Fragment
  protected def query(values: I): DoobieQuery[R] = new DoobieQuery[R](sql(values))
}

abstract class DoobieSingleResultFunction[I, R, E](implicit override val schema: DBSchema, val dbEngine: DoobiePgEngine)
  extends DBSingleResultFunction[I, R, DoobiePgEngine, IO] with DoobieFunction[I, R] {
}
