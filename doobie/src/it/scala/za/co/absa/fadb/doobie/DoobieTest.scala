package za.co.absa.fadb.doobie

import cats.effect.IO
import doobie.util.log.{LogEvent, LogHandler}
import doobie.util.transactor.Transactor
import za.co.absa.fadb.DBSchema

trait DoobieTest {
  import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits._

  object Runs extends DBSchema

  val printSqlLogHandler: LogHandler[IO] = new LogHandler[IO] {
    def run(logEvent: LogEvent): IO[Unit] =
      IO {
        println(logEvent.sql)
      }
  }

  protected val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.ds.PGSimpleDataSource",
    "jdbc:postgresql://localhost:5432/movies",
    "postgres",
    "postgres",
    Some(printSqlLogHandler)
  )
}
