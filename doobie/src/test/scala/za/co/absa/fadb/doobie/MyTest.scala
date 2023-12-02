package za.co.absa.fadb.doobie

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.{Get, Read}
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import doobie.util.log.{LogEvent, LogHandler}
import za.co.absa.fadb.status.handling.implementations.StandardStatusHandling

case class CreateActorRequestBody(firstName: String, lastName: String)

class CreateActor(implicit override val schema: DBSchema, override val dbEngine: DoobiePgEngine)
  extends DoobieSingleResultFunction[CreateActorRequestBody, Int, DoobiePgEngine] {

  override implicit val readR: Read[Int] = implicitly[Read[Int]]
  override implicit val getR: Get[Int] = implicitly[Get[Int]]

  override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("o_actor_id")

  override def sql(values: CreateActorRequestBody)(implicit read: Read[Int], get: Get[Int]): Fragment = {
    sql"SELECT * FROM $functionName('${values.firstName}', '${values.lastName}')"
  }
}

class DoobieTest extends AnyFunSuite {
  import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits._
  object Runs extends DBSchema

  test("DoobieTest") {
    val printSqlLogHandler: LogHandler[IO] = new LogHandler[IO] {
      def run(logEvent: LogEvent): IO[Unit] =
        IO {
          println(logEvent.sql)
        }
    }


    val transactor = Transactor.fromDriverManager[IO](
      driver = "org.postgresql.ds.PGSimpleDataSource", // JDBC driver classname
      url = "jdbc:postgresql://localhost:5432/movies", // Connect URL
      user = "postgres", // Database user name
      password = "postgres", // Database password
      logHandler = Some(printSqlLogHandler) // Don't setup logging for now. See Logging page for how to log events in detail
    )

    val createActor = new CreateActor()(Runs, new DoobiePgEngine(transactor))
    assert(createActor.functionName == "runs.create_actor")
    val actorId = createActor(CreateActorRequestBody("John", "Doe")).unsafeRunSync()
  }
}
