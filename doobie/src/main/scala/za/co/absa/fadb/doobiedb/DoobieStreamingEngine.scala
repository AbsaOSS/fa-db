package za.co.absa.fadb.doobiedb

import cats.effect.Async
import doobie.Transactor
import doobie.implicits.toDoobieStreamOps
import doobie.util.Read
import za.co.absa.fadb.DBStreamingEngine

class DoobieStreamingEngine[F[_]: Async](val transactor: Transactor[F], chunkSize: Int = 512) extends DBStreamingEngine[F] {

  type QueryType[R] = DoobieQuery[R]

  override def runStreaming[R](query: QueryType[R]): fs2.Stream[F, R] =
    executeStreamingQuery(query)(query.readR)

  private def executeStreamingQuery[R](query: QueryType[R])(implicit readR: Read[R]): fs2.Stream[F, R] = {
    query.fragment.query[R].streamWithChunkSize(chunkSize).transact(transactor)
  }

}
