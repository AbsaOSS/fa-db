package za.co.absa.fadb.doobie

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.log.{LogEvent, LogHandler}
import doobie.{Fragment, Transactor}
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieSingleResultFunctionWithStatusSupport
import za.co.absa.fadb.status.FunctionStatus
import za.co.absa.fadb.status.handling.implementations.StandardStatusHandling

class DoobieFunctionWithStatusSupportTest extends AnyFunSuite {

  case class CreateActorRequestBody(firstName: String, lastName: String)

  class CreateActor(implicit schema: DBSchema, dbEngine: DoobiePgEngine)
    extends DoobieSingleResultFunctionWithStatusSupport[CreateActorRequestBody, Int]
    with StandardStatusHandling {

    override def sql(values: CreateActorRequestBody)(implicit read: Read[Int]): Fragment =
      sql"SELECT * FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
  }

  import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits._
  object Runs extends DBSchema

  val printSqlLogHandler: LogHandler[IO] = new LogHandler[IO] {
    def run(logEvent: LogEvent): IO[Unit] =
      IO {
        println(logEvent.sql)
      }
  }

  private val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.ds.PGSimpleDataSource",
    "jdbc:postgresql://localhost:5432/movies",
    "postgres",
    "postgres",
    Some(printSqlLogHandler)
  )

  private val createActor = new CreateActor()(Runs, new DoobiePgEngine(transactor))

  test("DoobieTest with status handling") {
    val requestBody = CreateActorRequestBody("Pavel", "Marek")
    createActor.applyWithStatus(requestBody).unsafeRunSync() match {
      case Right(success) =>
        assert(success.functionStatus == FunctionStatus(11, "Actor created"))
      case Left(failure) =>
        fail(failure.failure)
    }
  }


}
