package za.co.absa.fadb.doobie

import cats.effect.unsafe.implicits.global
import doobie.Fragment
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieSingleResultFunctionWithStatusSupport
import za.co.absa.fadb.status.FunctionStatus
import za.co.absa.fadb.status.handling.implementations.StandardStatusHandling

class DoobieSingleResultFunctionWithStatusSupportTest extends AnyFunSuite with DoobieTest {

  class CreateActor(implicit schema: DBSchema, dbEngine: DoobiePgEngine)
    extends DoobieSingleResultFunctionWithStatusSupport[CreateActorRequestBody, Int]
    with StandardStatusHandling {

    override def sql(values: CreateActorRequestBody)(implicit read: Read[Int]): Fragment =
      sql"SELECT status, status_text, o_actor_id FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
  }

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
