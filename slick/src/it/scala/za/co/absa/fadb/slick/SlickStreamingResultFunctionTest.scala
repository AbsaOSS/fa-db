package za.co.absa.fadb.slick

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.SQLActionBuilder
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.SlickFunction.SlickStreamingResultFunction
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._

class SlickStreamingResultFunctionTest extends AnyFunSuite with SlickTest {

  class GetActors(implicit override val schema: DBSchema, val dbEngine: SlickPgStreamingEngine[IO])
    extends SlickStreamingResultFunction[GetActorsQueryParameters, Actor, IO]
      with ActorSlickConverter {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("actor_id", "first_name", "last_name")

    override protected def sql(values: GetActorsQueryParameters): SQLActionBuilder = {
      sql"""SELECT #$selectEntry FROM #$functionName(${values.firstName},${values.lastName}) #$alias;"""
    }
  }

  private val getActors = new GetActors()(Runs, new SlickPgStreamingEngine[IO](db))

  test("Retrieving actors from database") {
    val expectedResultElem = Actor(49, "Pavel", "Marek")
    val results = getActors(GetActorsQueryParameters(Some("Pavel"), Some("Marek"))).take(10).compile.toList.unsafeRunSync()
    assert(results.contains(expectedResultElem))
  }

}
