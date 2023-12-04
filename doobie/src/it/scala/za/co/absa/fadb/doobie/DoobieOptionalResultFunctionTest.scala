package za.co.absa.fadb.doobie

import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieOptionalResultFunction

class DoobieOptionalResultFunctionTest extends AnyFunSuite with DoobieTest {

  case class Actor(actorId: Int, firstName: String, lastName: String)

  class GetActorById(implicit schema: DBSchema, dbEngine: DoobiePgEngine)
    extends DoobieOptionalResultFunction[Int, Actor] {

    override def sql(values: Int)(implicit read: Read[Actor]): Fragment =
      sql"SELECT actor_id, first_name, last_name FROM ${Fragment.const(functionName)}($values)"
  }

  private val createActor = new GetActorById()(Runs, new DoobiePgEngine(transactor))

  test("DoobieTest") {
    val expectedResult = Some(Actor(49, "Pavel", "Marek"))
    val result = createActor.apply(49).unsafeRunSync()
    assert(expectedResult == result)
  }

}
