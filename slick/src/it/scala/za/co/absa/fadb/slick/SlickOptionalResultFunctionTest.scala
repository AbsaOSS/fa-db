package za.co.absa.fadb.slick

import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.SQLActionBuilder
import za.co.absa.fadb.DBFunction.DBOptionalResultFunction
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class SlickOptionalResultFunctionTest extends AnyFunSuite with SlickTest {

  class GetActorById(implicit override val schema: DBSchema, val dbEngine: SlickPgEngine)
    extends DBOptionalResultFunction[Int, Actor, SlickPgEngine, Future]
      with SlickFunction[Int, Actor]
      with ActorSlickConverter {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("actor_id", "first_name", "last_name")

    override protected def sql(values: Int): SQLActionBuilder = {
      sql"""SELECT #$selectEntry FROM #$functionName($values) #$alias;"""
    }
  }

  private val getActorById = new GetActorById()(Runs, new SlickPgEngine(db))

  test("SlickTest") {
    val expectedResultElem = Some(Actor(49, "Pavel", "Marek"))
    val results = getActorById(49)
    assert(Await.result(results, 5.seconds) == expectedResultElem)
  }
}
