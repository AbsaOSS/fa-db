package za.co.absa.fadb

abstract class DBStreamingEngine[F[_]] {

  type QueryType[R] <: Query[R]

  def runStreaming[R](query: QueryType[R]): fs2.Stream[F, R]

}
