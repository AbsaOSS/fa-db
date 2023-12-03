package za.co.absa.fadb.doobie

import cats.effect.IO
import doobie._
import doobie.implicits._
import za.co.absa.fadb.DBEngine

class DoobiePgEngine(val transactor: Transactor[IO]) extends DBEngine[IO] {
  type QueryType[T] = DoobieQuery[T]

  private def run_[R](query: QueryType[R])(implicit readR: Read[R]): IO[Seq[R]] = {
    query.fragment.query[R].to[Seq].transact(transactor)
  }

  override def run[R](query: DoobieQuery[R]): IO[Seq[R]] =
    run_(query)(query.readR)
}
