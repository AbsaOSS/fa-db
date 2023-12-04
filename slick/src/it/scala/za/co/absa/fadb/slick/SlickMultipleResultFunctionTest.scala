package za.co.absa.fadb.slick

import cats.implicits._
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.SQLActionBuilder
import za.co.absa.fadb.DBFunction.DBMultipleResultFunction
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class SlickMultipleResultFunctionTest extends AnyFunSuite with SlickTest {

  class GetActors(implicit override val schema: DBSchema, val dbEngine: SlickPgEngine)
    extends DBMultipleResultFunction[GetActorsQueryParameters, Actor, SlickPgEngine, Future]
      with SlickFunction[GetActorsQueryParameters, Actor]
      with ActorSlickConverter {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("actor_id", "first_name", "last_name")

    override protected def sql(values: GetActorsQueryParameters): SQLActionBuilder = {
      sql"""SELECT #$selectEntry FROM #$functionName(${values.firstName},${values.lastName}) #$alias;"""
    }
  }

  private val getActors = new GetActors()(Runs, new SlickPgEngine(db))

  test("SlickTest") {
    val expectedResultElem = Actor(49, "Pavel", "Marek")
    val results = getActors.apply(GetActorsQueryParameters(Some("Pavel"), Some("Marek")))
    assert(Await.result(results, 5.seconds).contains(expectedResultElem))
  }
}
