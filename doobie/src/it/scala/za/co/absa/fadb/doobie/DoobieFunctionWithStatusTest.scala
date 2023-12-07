package za.co.absa.fadb.doobie

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieSingleResultFunctionWithStatus

class DoobieFunctionWithStatusTest extends AnyFunSuite with DoobieTest {
  class CreateActor(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieSingleResultFunctionWithStatus[CreateActorRequestBody, Int, IO] {

    override def sql(values: CreateActorRequestBody)(implicit read: Read[StatusWithData[Int]]): Fragment =
      sql"SELECT status, status_text, o_actor_id FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
  }

  test("whatever") {
    val createActor = new CreateActor()(Runs, new DoobieEngine(transactor))
    val result = createActor(CreateActorRequestBody("Pavel", "Marek")).unsafeRunSync()
    println(result)
    assert(result.isRight)
  }
}
