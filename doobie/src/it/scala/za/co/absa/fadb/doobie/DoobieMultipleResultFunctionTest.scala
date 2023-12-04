package za.co.absa.fadb.doobie

import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.DoobieFunction.DoobieMultipleResultFunction

class DoobieMultipleResultFunctionTest extends AnyFunSuite with DoobieTest {

  case class Actor(actorId: Int, firstName: String, lastName: String)
  case class GetActorsQueryParameters(firstName: Option[String], lastName: Option[String])

  class GetActors(implicit schema: DBSchema, dbEngine: DoobiePgEngine)
    extends DoobieMultipleResultFunction[GetActorsQueryParameters, Actor] {

    override def sql(values: GetActorsQueryParameters)(implicit read: Read[Actor]): Fragment =
      sql"SELECT actor_id, first_name, last_name FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
  }

  private val getActors = new GetActors()(Runs, new DoobiePgEngine(transactor))

  test("DoobieTest") {
    val expectedResultElem = Actor(49, "Pavel", "Marek")
    val results = getActors.apply(GetActorsQueryParameters(Some("Pavel"), Some("Marek"))).unsafeRunSync()
    assert(results.contains(expectedResultElem))
  }

}
