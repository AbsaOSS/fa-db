package za.co.absa.fadb.doobie

import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieSingleResultFunction

class DoobieSingleResultFunctionTest extends AnyFunSuite with DoobieTest {

  class CreateActor(implicit schema: DBSchema, dbEngine: DoobiePgEngine)
    extends DoobieSingleResultFunction[CreateActorRequestBody, Int] {

    override def sql(values: CreateActorRequestBody)(implicit read: Read[Int]): Fragment =
      sql"SELECT o_actor_id FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
  }

  private val createActor = new CreateActor()(Runs, new DoobiePgEngine(transactor))

  test("DoobieTest") {
    assert(createActor(CreateActorRequestBody("Pavel", "Marek")).unsafeRunSync().isInstanceOf[Int])
  }
}
