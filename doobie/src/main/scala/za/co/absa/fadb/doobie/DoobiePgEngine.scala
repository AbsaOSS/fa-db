package za.co.absa.fadb.doobie

import cats.effect.IO
import doobie._
import doobie.implicits._
import za.co.absa.fadb.DBEngine

/**
 *  `DoobiePgEngine` is a class that extends `DBEngine` with `IO` as the effect type.
 *  It uses Doobie's `Transactor[IO]` to execute SQL queries.
 *
 *  @param transactor the Doobie transactor for executing SQL queries
 */
class DoobiePgEngine(val transactor: Transactor[IO]) extends DBEngine[IO] {

  /** The type of Doobie queries that produce `T` */
  type QueryType[T] = DoobieQuery[T]

  /**
   *  Executes a Doobie query and returns the result as an `IO[Seq[R]]`.
   *
   *  @param query the Doobie query to execute
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @return the query result as an `IO[Seq[R]]`
   */
  private def executeQuery[R](query: QueryType[R])(implicit readR: Read[R]): IO[Seq[R]] = {
    query.fragment.query[R].to[Seq].transact(transactor)
  }

  /**
   *  Runs a Doobie query and returns the result as an `IO[Seq[R]]`.
   *
   *  @param query the Doobie query to run
   *  @return the query result as an `IO[Seq[R]]`
   */
  override def run[R](query: QueryType[R]): IO[Seq[R]] =
    executeQuery(query)(query.readR)
}
