package za.co.absa.fadb.doobie

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.Read._
import doobie.util.fragment.Fragment
import doobie.util.log.{LogEvent, LogHandler}
import doobie.util.transactor.Transactor
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.{DoobieSingleResultFunction, DoobieSingleResultFunctionWithStatusSupport}
import za.co.absa.fadb.status.FunctionStatus

case class CreateActorRequestBody(firstName: String, lastName: String)
case class IntWithStatusAndStatusText(status: Int, status_text: String, o_actor_id: Int)

class CreateActor(implicit override val schema: DBSchema, override val dbEngine: DoobiePgEngine)
  extends DoobieSingleResultFunction[CreateActorRequestBody, IntWithStatusAndStatusText] {

  override implicit val readR: Read[IntWithStatusAndStatusText] = Read[(Int, String, Int)].map {
    case (status, statusText, actorId) => IntWithStatusAndStatusText(status, statusText, actorId)
  }

  // override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("o_actor_id")

  override def sql(values: CreateActorRequestBody)(implicit read: Read[IntWithStatusAndStatusText]): Fragment = {
    sql"SELECT status, status_text, o_actor_id FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
  }
}

class CreateActorWithStatusHandling(functionNameOverride: Option[String] = None)(implicit override val schema: DBSchema, override val dbEngine: DoobiePgEngine)
  extends DoobieSingleResultFunctionWithStatusSupport[CreateActorRequestBody, Int](functionNameOverride) {

  override def sql(values: CreateActorRequestBody)(implicit read: Read[Int]): Fragment =
    sql"SELECT status, status_text, o_actor_id FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
}

class DoobieTest extends AnyFunSuite {
  import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits._
  object Runs extends DBSchema

  val printSqlLogHandler: LogHandler[IO] = new LogHandler[IO] {
    def run(logEvent: LogEvent): IO[Unit] =
      IO {
        println(logEvent.sql)
      }
  }

  val transactor = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.ds.PGSimpleDataSource",
    url = "jdbc:postgresql://localhost:5432/movies",
    user = "postgres",
    password = "postgres",
    logHandler = Some(printSqlLogHandler)
  )

  test("DoobieTest") {
    val createActor = new CreateActor()(Runs, new DoobiePgEngine(transactor))
    assert(createActor.functionName == "runs.create_actor")
    val actorId = createActor(CreateActorRequestBody("Dominika", "Salamonova")).unsafeRunSync()
    assert(actorId.status == 11)
    assert(actorId.status_text == "Actor created")
  }

  test("DoobieTest with status handling") {
    val createActorWithStatusHandling = new CreateActorWithStatusHandling(Some("create_actor"))(Runs, new DoobiePgEngine(transactor))
    createActorWithStatusHandling.applyWithStatusHandling(CreateActorRequestBody("Marek", "Salamon")).unsafeRunSync() match {
      case Right(success) =>
        assert(success.functionStatus == FunctionStatus(11, "Actor created"))
      case Left(failure) =>
        fail(failure.failure)
    }
  }
}
