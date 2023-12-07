package za.co.absa.fadb.slick

import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.slick.SlickFunction.SlickSingleResultFunctionWithStatus
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class SlickSingleResultFunctionWithStatusTest extends AnyFunSuite with SlickTest {
  class CreateActor(implicit schema: DBSchema, dbEngine: SlickPgEngine)
    extends SlickSingleResultFunctionWithStatus[CreateActorRequestBody, Int] {

    override def fieldsToSelect: Seq[String] = super.fieldsToSelect ++ Seq("o_actor_id")

    override protected def sql(values: CreateActorRequestBody): SQLActionBuilder =
      sql"""SELECT #$selectEntry FROM #$functionName(${values.firstName},${values.lastName}) #$alias;"""

    /**
     * The `GetResult[R]` instance used to read the query result into `R`.
     */
    override protected def slickConverter: GetResult[Int] = GetResult(r => r.<<)
  }

  private val createActor = new CreateActor()(Runs, new SlickPgEngine(db))

  test("SlickTest with status handling") {
    val requestBody = CreateActorRequestBody("Pavel", "Marek")
    val result = createActor(requestBody)
    assert(Await.result(result, 5.seconds).isRight)
    println(result)
  }
}
