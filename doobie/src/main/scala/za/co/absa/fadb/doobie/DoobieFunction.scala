/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.fadb.doobie

import cats.MonadError
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import za.co.absa.fadb.DBFunction._
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.status.{FailedOrRow, Row}

import scala.language.higherKinds

trait DoobieFunctionBase[R] {

  /**
   *  The `Read[R]` instance used to read the query result into `R`.
   */
  implicit val readR: Read[R]

  protected def selectEntry: String
  protected def functionName: String
  protected def alias: String

  /**
   * Composes individual fragments into a single final fragment
   * @param fragments fragments to compose
   * @return composed fragment
   */
  protected final def composeFragments(fragments: Seq[Fragment]): Fragment = {
    val args = fragments.toList match {
      case head :: tail => tail.foldLeft(head)((acc, frag) => acc ++ fr"," ++ frag)
      case Nil          => fr""
    }
    sql"SELECT ${Fragment.const(selectEntry)} FROM ${Fragment.const(functionName)}($args) ${Fragment.const(alias)};"
  }
}

/**
 *  `DoobieFunction` provides support for executing database functions using Doobie.
 *
 *  @tparam I the input type of the function
 *  @tparam R the result type of the function
 */
trait DoobieFunction[I, R, F[_]] extends DoobieFunctionBase[R] {

  /**
   *  Function that generates a sequence of `Fragment`s representing the SQL query from input values for the function.
   *  @return the sequence of `Fragment`s representing the SQL query
   */
  def toFragmentsSeq: I => Seq[Fragment]

  /**
   *  Generates a `Fragment` representing the SQL query for the function.
   *  @param values the input values for the function
   *  @param selectEntry columns names for the select statement
   *  @param functionName name of the function
   *  @param alias alias for the sql query
   *  @param read Read instance for R type
   *  @param me MonadError instance for F type
   *  @return the `Fragment` representing the SQL query
   */
  private def meSql(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    read: Read[R],
    me: MonadError[F, Throwable]
  ): F[Fragment] = {
    me.catchNonFatal {
      val fragments = toFragmentsSeq(values)
      composeFragments(fragments)
    }
  }

  /**
   *  Generates a `DoobieQuery[R]` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the `DoobieQuery[R]` representing the SQL query
   */
  private def queryWithAllInputParams(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    me: MonadError[F, Throwable]
  ): F[DoobieQuery[R]] = {
    me.flatMap(meSql(values, selectEntry, functionName, alias))(fr => me.pure(new DoobieQuery[R](fr)))
  }

  /**
   *  Generates a `DoobieQuery[R]` representing the SQL query for the function.
   *  @param values the input values for the function
   *  @param me MonadError instance for F type
   *  @return the `DoobieQuery[R]` representing the SQL query
   */
  protected def query(values: I)(implicit me: MonadError[F, Throwable]): F[DoobieQuery[R]] = {
    queryWithAllInputParams(values, selectEntry, functionName, alias)
  }

  /**
   *  Generates a `Fragment` representing the SQL query for the function.
   *  @param values the input values for the function
   *  @param read Read instance for `R` type
   *  @param me MonadError instance for F type
   *  @return the `Fragment` representing the SQL query
   */
  protected final def sql(values: I)(implicit read: Read[R], me: MonadError[F, Throwable]): F[Fragment] = {
    meSql(values, selectEntry, functionName, alias)(read, me)
  }

}

trait DoobieFunctionWithStatus[I, R, F[_]] extends DoobieFunctionBase[R] {

  /**
   *  The `Read[StatusWithData[R]]` instance used to read the query result with status into `StatusWithData[R]`.
   */
  implicit def readStatusWithDataR(implicit readR: Read[R]): Read[StatusWithData[R]] = Read[(Int, String, R)].map {
    case (status, status_text, data) => StatusWithData(status, status_text, data)
  }

  /**
   *  Function that generates a sequence of `Fragment`s representing the SQL query from input values for the function.
   *  @return the sequence of `Fragment`s representing the SQL query
   */
  def toFragmentsSeq: I => Seq[Fragment]

  /**
   *  Generates a `Fragment` representing the SQL query for the function.
   *  @param values the input values for the function
   *  @param selectEntry columns names for the select statement
   *  @param functionName name of the function
   *  @param alias alias for the sql query
   *  @param read Read instance for R type
   *  @param me MonadError instance for F type
   *  @return the `Fragment` representing the SQL query
   */
  private def meSql(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    read: Read[StatusWithData[R]],
    me: MonadError[F, Throwable]
  ): F[Fragment] = {
    me.catchNonFatal {
      val fragments = toFragmentsSeq(values)
      composeFragments(fragments)
    }
  }

  /**
   * Generates a `DoobieQueryWithStatus[R]` representing the SQL query for the function.
   * @param values the input values for the function
   * @param me MonadError instance for F type
   * @return the `DoobieQueryWithStatus[R]` representing the SQL query
   */
  protected def query(values: I)(implicit me: MonadError[F, Throwable]): F[DoobieQueryWithStatus[R]] = {
    queryWithAllInputParams(values, selectEntry, functionName, alias)
  }

  /**
   *  Generates a `DoobieQueryWithStatus[R]` representing the SQL query for the function.
   *  @param values the input values for the function
   *  @param selectEntry columns names for the select statement
   *  @param functionName name of the function
   *  @param alias alias for the sql query
   *  @param me MonadError instance for F type
   *  @return the `DoobieQueryWithStatus[R]` representing the SQL query
   */
  private def queryWithAllInputParams(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    me: MonadError[F, Throwable]
  ): F[DoobieQueryWithStatus[R]] = {
    me.flatMap(meSql(values, selectEntry, functionName, alias))(fr =>
      me.pure(new DoobieQueryWithStatus[R](fr, checkStatus))
    )
  }

  /**
   *  Generates a `Fragment` representing the SQL query for the function.
   *  @param values the input values for the function
   *  @param read Read instance for `StatusWithData[R]`
   *  @param me MonadError instance for F type
   *  @return the `Fragment` representing the SQL query
   */
  protected final def sql(
    values: I
  )(implicit read: Read[StatusWithData[R]], me: MonadError[F, Throwable]): F[Fragment] = {
    meSql(values, selectEntry, functionName, alias)(read, me)
  }

  // This is to be mixed in by an implementation of StatusHandling
  def checkStatus[D](statusWithData: Row[D]): FailedOrRow[D]
}

/**
 *  `DoobieFunction` is an object that contains several abstract classes extending different types of db functions.
 *  These classes use Doobie's `Fragment` to represent SQL queries and `DoobieEngine` to execute them.
 */
object DoobieFunction {

  /**
   *  `DoobieSingleResultFunction` represents a db function that returns a single result.
   *
   *  @param toFragmentsSeq a function that generates a sequence of `Fragment`s
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobieEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
   */
  abstract class DoobieSingleResultFunction[I, R, F[_]](
    override val toFragmentsSeq: I => Seq[Fragment],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBSingleResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R, F]

  /**
    *  `DoobieSingleResultFunctionWithStatus` represents a db function that returns a single result with status.
    *
    *  @param toFragmentsSeq a function that generates a sequence of `Fragment`s
    *  @param functionNameOverride the optional override for the function name
    *  @param schema the database schema
    *  @param dbEngine the `DoobieEngine` instance used to execute SQL queries
    *  @param readR Read instance for `R`
    *  @param readSelectWithStatus Read instance for `StatusWithData[R]`
    *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
    */
  abstract class DoobieSingleResultFunctionWithStatus[I, R, F[_]](
    override val toFragmentsSeq: I => Seq[Fragment],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R],
    val readSelectWithStatus: Read[StatusWithData[R]]
  ) extends DBSingleResultFunctionWithStatus[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunctionWithStatus[I, R, F]

  /**
   *  `DoobieMultipleResultFunction` represents a db function that returns multiple results.
   */
  abstract class DoobieMultipleResultFunction[I, R, F[_]](
    override val toFragmentsSeq: I => Seq[Fragment],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBMultipleResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R, F]

  /**
    *  `DoobieMultipleResultFunctionWithStatus` represents a db function that returns multiple results with statuses.
    */
  abstract class DoobieMultipleResultFunctionWithStatus[I, R, F[_]](
   override val toFragmentsSeq: I => Seq[Fragment],
   functionNameOverride: Option[String] = None
  )(implicit
   override val schema: DBSchema,
   val dbEngine: DoobieEngine[F],
   val readR: Read[R]
  ) extends DBMultipleResultFunctionWithStatus[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunctionWithStatus[I, R, F]

  /**
   *  `DoobieOptionalResultFunction` represents a db function that returns an optional result.
   */
  abstract class DoobieOptionalResultFunction[I, R, F[_]](
    override val toFragmentsSeq: I => Seq[Fragment],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBOptionalResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R, F]

  /**
    *  `DoobieOptionalResultFunctionWithStatus` represents a db function that returns an optional result.
    */
  abstract class DoobieOptionalResultFunctionWithStatus[I, R, F[_]](
    override val toFragmentsSeq: I => Seq[Fragment],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBOptionalResultFunctionWithStatus[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunctionWithStatus[I, R, F]
}
