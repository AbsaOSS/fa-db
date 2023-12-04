package za.co.absa.fadb.doobie

import cats.effect.IO
import doobie.util.Read
import doobie.util.fragment.Fragment
import za.co.absa.fadb.DBFunction._
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobie.Results.{FailedResult, ResultWithStatus, SuccessfulResult}
import za.co.absa.fadb.status.FunctionStatus
import za.co.absa.fadb.status.handling.StatusHandling

import scala.util.{Failure, Success}

/**
 *  `DoobieFunction` provides support for executing database functions using Doobie.
 *
 *  @tparam I the input type of the function
 *  @tparam R the result type of the function
 */
private [doobie] trait DoobieFunction[I, R] {

  /**
   *  The `Read[R]` instance used to read the query result into `R`.
   */
  implicit val readR: Read[R]

  /**
   *  Generates a Doobie `Fragment` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the Doobie `Fragment` representing the SQL query
   */
  def sql(values: I)(implicit read: Read[R]): Fragment

  /**
   *  Generates a `DoobieQuery[R]` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the `DoobieQuery[R]` representing the SQL query
   */
  protected def query(values: I): DoobieQuery[R] = new DoobieQuery[R](sql(values))
}

/**
 *  `DoobieFunctionWithStatusSupport` provides support for executing database functions with status handling using Doobie.
 *
 *  @tparam I the input type of the function
 *  @tparam R the result type of the function
 */
private [doobie] trait DoobieFunctionWithStatusSupport[I, R] extends StatusHandling {

  /**
   *  The `Read[R]` instance used to read the query result into `R`.
   */
  implicit val readR: Read[R]

  /**
   *  The `Read[(Int, String, R)]` instance used to read the query result with status into `(Int, String, R)`.
   */
  implicit val readSelectWithStatus: Read[(Int, String, R)]

  /**
   *  Generates a Doobie `Fragment` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the Doobie `Fragment` representing the SQL query
   */
  def sql(values: I)(implicit read: Read[R]): Fragment

  /**
   *  Generates a `DoobieQuery[(Int, String, R)]` representing the SQL query for the function with status.
   *
   *  @param values the input values for the function
   *  @return the `DoobieQuery[(Int, String, R)]` representing the SQL query with status
   */
  protected def query(values: I): DoobieQuery[(Int, String, R)] = new DoobieQuery[(Int, String, R)](sql(values))
}

/**
 *  `DoobieFunction` is an object that contains several abstract classes extending different types of database functions.
 *  These classes use Doobie's `Fragment` to represent SQL queries and `DoobiePgEngine` to execute them.
 */
object DoobieFunction {

  /**
   *  `DoobieSingleResultFunction` is an abstract class that extends `DBSingleResultFunction` with `DoobiePgEngine` as the engine type and `IO` as the effect type.
   *  It represents a database function that returns a single result.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobiePgEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   */
  abstract class DoobieSingleResultFunction[I, R](functionNameOverride: Option[String] = None)(implicit
    override val schema: DBSchema,
    val dbEngine: DoobiePgEngine,
    val readR: Read[R]
  ) extends DBSingleResultFunction[I, R, DoobiePgEngine, IO](functionNameOverride)
      with DoobieFunction[I, R] {}

  /**
   *  `DoobieMultipleResultFunction` is an abstract class that extends `DBMultipleResultFunction` with `DoobiePgEngine` as the engine type and `IO` as the effect type.
   *  It represents a database function that returns multiple results.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobiePgEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   */
  abstract class DoobieMultipleResultFunction[I, R](functionNameOverride: Option[String] = None)(implicit
    override val schema: DBSchema,
    val dbEngine: DoobiePgEngine,
    val readR: Read[R]
  ) extends DBMultipleResultFunction[I, R, DoobiePgEngine, IO](functionNameOverride)
      with DoobieFunction[I, R] {}

  /**
   *  `DoobieOptionalResultFunction` is an abstract class that extends `DBOptionalResultFunction` with `DoobiePgEngine` as the engine type and `IO` as the effect type.
   *  It represents a database function that returns an optional result.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobiePgEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   */
  abstract class DoobieOptionalResultFunction[I, R](functionNameOverride: Option[String] = None)(implicit
    override val schema: DBSchema,
    val dbEngine: DoobiePgEngine,
    val readR: Read[R]
  ) extends DBOptionalResultFunction[I, R, DoobiePgEngine, IO](functionNameOverride)
      with DoobieFunction[I, R] {}

  /**
   *  `DoobieSingleResultFunctionWithStatusSupport` is an abstract class that extends `DBSingleResultFunction` with `DoobiePgEngine` as the engine type and `IO` as the effect type.
   *  It represents a database function that returns a single result and supports function status.
   *
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobiePgEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @param readSelectWithStatus the `Read[(Int, String, R)]` instance used to read the query result with status into `(Int, String, R)`
   */
  abstract class DoobieSingleResultFunctionWithStatusSupport[I, R](functionNameOverride: Option[String] = None)(implicit
    override val schema: DBSchema,
    val dbEngine: DoobiePgEngine,
    val readR: Read[R],
    val readSelectWithStatus: Read[(Int, String, R)]
  ) extends DBSingleResultFunction[I, (Int, String, R), DoobiePgEngine, IO](functionNameOverride)
      with DoobieFunctionWithStatusSupport[I, R] {

    /**
     *  Executes the function with the given input values and returns the result with status.
     *
     *  @param values the input values for the function
     *  @return the result with status
     */
    def applyWithStatus(values: I): IO[ResultWithStatus[R]] = {
      super.apply(values).flatMap { case (status, statusText, result) =>
        checkStatus(status, statusText) match {
          case Success(_) => IO.pure(Right(SuccessfulResult[R](FunctionStatus(status, statusText), result)))
          case Failure(e) => IO.pure(Left(FailedResult(FunctionStatus(status, statusText), e)))
        }
      }
    }
  }

  // for an example see DoobieFunctionWithStatusSupportTest

}
