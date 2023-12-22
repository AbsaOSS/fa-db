package za.co.absa.fadb.doobiedb

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobiedb.DoobieFunction.DoobieStreamingResultFunction

class DoobieStreamingResultFunctionTest extends AnyFunSuite with DoobieTest {

  class GetActors(implicit schema: DBSchema, dbEngine: DoobieStreamingEngine[IO])
    extends DoobieStreamingResultFunction[GetActorsQueryParameters, Actor, IO] {

    override def sql(values: GetActorsQueryParameters)(implicit read: Read[Actor]): Fragment =
      sql"SELECT actor_id, first_name, last_name FROM ${Fragment.const(functionName)}(${values.firstName}, ${values.lastName})"
  }

  private val getActors = new GetActors()(Runs, new DoobieStreamingEngine(transactor))

  test("Retrieving actor from database") {
    val expectedResultElem = Actor(49, "Pavel", "Marek")
    val results = getActors(GetActorsQueryParameters(Some("Pavel"), Some("Marek"))).take(10).compile.toList.unsafeRunSync()
    assert(results.contains(expectedResultElem))
  }

}
