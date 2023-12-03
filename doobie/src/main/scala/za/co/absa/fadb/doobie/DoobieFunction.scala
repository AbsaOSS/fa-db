package za.co.absa.fadb.doobie

import cats.effect.IO
import doobie.util.Read
import doobie.util.fragment.Fragment
import za.co.absa.fadb.DBFunction._
import za.co.absa.fadb.status.FunctionStatus
import za.co.absa.fadb.status.handling.implementations.StandardStatusHandling
import za.co.absa.fadb.{DBFunctionFabric, DBSchema}

import scala.util.{Failure, Success}


trait DoobieFunction[I, R] extends DBFunctionFabric {
  implicit val readR: Read[R]
  def sql(values: I)(implicit read: Read[R]): Fragment
  protected def query(values: I): DoobieQuery[R] = new DoobieQuery[R](sql(values))
}

trait DoobieFunctionWithStatusSupport[I, R] extends DBFunctionFabric with StandardStatusHandling {
  implicit val readR: Read[R]
  implicit val readSelectWithStatus: Read[(Int, String, R)]
  def sql(values: I)(implicit read: Read[R]): Fragment
  protected def query(values: I): DoobieQuery[(Int, String, R)] = new DoobieQuery[(Int, String, R)](sql(values))
}

object DoobieFunction {
  abstract class DoobieSingleResultFunction[I, R](functionNameOverride: Option[String] = None)(implicit override val schema: DBSchema, val dbEngine: DoobiePgEngine, val readR: Read[R])
    extends DBSingleResultFunction[I, R, DoobiePgEngine, IO](functionNameOverride) with DoobieFunction[I, R] {
  }

  abstract class DoobieMultipleResultFunction[I, R](functionNameOverride: Option[String] = None)(implicit override val schema: DBSchema, val dbEngine: DoobiePgEngine, val readR: Read[R])
    extends DBMultipleResultFunction[I, R, DoobiePgEngine, IO](functionNameOverride) with DoobieFunction[I, R] {
  }

  abstract class DoobieOptionalResultFunction[I, R](functionNameOverride: Option[String] = None)(implicit override val schema: DBSchema, val dbEngine: DoobiePgEngine, val readR: Read[R])
    extends DBOptionalResultFunction[I, R, DoobiePgEngine, IO](functionNameOverride) with DoobieFunction[I, R] {
  }

  case class SuccessfulResult[R](functionStatus: FunctionStatus, result: R)
  case class FailedResult(functionStatus: FunctionStatus, failure: Throwable)

  type OutcomeWithStatus[R] = Either[FailedResult, SuccessfulResult[R]]

  abstract class DoobieSingleResultFunctionWithStatusSupport[I, R](functionNameOverride: Option[String] = None)(implicit override val schema: DBSchema, val dbEngine: DoobiePgEngine, val readR: Read[R], val readSelectWithStatus: Read[(Int, String, R)])
    extends DBSingleResultFunction[I, (Int, String, R), DoobiePgEngine, IO](functionNameOverride) with DoobieFunctionWithStatusSupport[I, R] {

    def applyWithStatusHandling(values: I): IO[OutcomeWithStatus[R]] = {
      super.apply(values).flatMap {
        case (status, statusText, result) =>
          checkStatus(status, statusText) match {
            case Success(_) => IO.pure(Right(SuccessfulResult[R](FunctionStatus(status, statusText), result)))
            case Failure(e) => IO.pure(Left(FailedResult(FunctionStatus(status, statusText), e)))
          }
      }
    }
  }
}
