package za.co.absa.fadb.slick

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._
import za.co.absa.fadb.slick.SlickFunction.SlickSingleResultFunctionWithStatus
import za.co.absa.fadb.status.handling.implementations.StandardQueryStatusHandling

import scala.concurrent.ExecutionContext.Implicits.global

class SlickSingleResultFunctionWithStatusTest extends AnyFunSuite with SlickTest with ScalaFutures {
  class CreateActor(implicit schema: DBSchema, dbEngine: SlickPgEngine)
      extends SlickSingleResultFunctionWithStatus[CreateActorRequestBody, Int]
      with StandardQueryStatusHandling {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("o_actor_id")

    override protected def sql(values: CreateActorRequestBody): SQLActionBuilder =
      sql"""SELECT #$selectEntry FROM #$functionName(${values.firstName},${values.lastName}) #$alias;"""

    override protected def slickConverter: GetResult[Int] = GetResult(r => r.<<)
  }

  private val createActor = new CreateActor()(Runs, new SlickPgEngine(db))

  test("SlickTest with status handling") {
    val requestBody = CreateActorRequestBody("Pavel", "Marek")
    val result = createActor(requestBody).futureValue
    assert(result.isRight)
  }
}
