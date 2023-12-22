package za.co.absa.fadb.slick

import cats.effect.Async
import fs2.interop.reactivestreams.PublisherOps
import slick.jdbc.JdbcBackend.Database
import za.co.absa.fadb.DBStreamingEngine

class SlickPgStreamingEngine[F[_]: Async](val db: Database, chunkSize: Int = 512) extends DBStreamingEngine[F] {

  type QueryType[R] = SlickQuery[R]

  def runStreaming[R](query: QueryType[R]): fs2.Stream[F, R] = {
    val slickPublisher = db.stream(query.sql.as[R](query.getResult))
    slickPublisher.toStreamBuffered[F](chunkSize)
  }

}
