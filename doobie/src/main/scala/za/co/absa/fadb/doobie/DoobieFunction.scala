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

import cats._
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import za.co.absa.fadb.DBFunction._
import za.co.absa.fadb.exceptions.StatusException
import za.co.absa.fadb.{DBFunctionWithStatus, DBSchema, FunctionStatusWithData}

import scala.language.higherKinds

trait DoobieFunctionBase[R] {

  /**
   *  The `Read[R]` instance used to read the query result into `R`.
   */
  implicit val readR: Read[R]

  protected def selectEntry: String
  protected def functionName: String
  protected def alias: String
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

  def meSql(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    read: Read[R],
    ME: MonadError[F, Throwable]
  ): F[Fragment] = {
    ME.catchNonFatal {
      val fragments = toFragmentsSeq(values)
      val args = fragments.toList match {
        case head :: tail => tail.foldLeft(head)((acc, frag) => acc ++ fr"," ++ frag)
        case Nil          => fr""
      }
      sql"SELECT ${Fragment.const(selectEntry)} FROM ${Fragment.const(functionName)}($args) ${Fragment.const(alias)};"
    }
  }

  /**
   *  Generates a `DoobieQuery[R]` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the `DoobieQuery[R]` representing the SQL query
   */
  protected def queryWithAllInputParams(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    ME: MonadError[F, Throwable]
  ): F[DoobieQuery[R]] = {
    ME.flatMap(meSql(values, selectEntry, functionName, alias))(fr => ME.pure(new DoobieQuery[R](fr)))
  }

  protected def query(values: I)(implicit ME: MonadError[F, Throwable]): F[DoobieQuery[R]] = {
    queryWithAllInputParams(values, selectEntry, functionName, alias)
  }

  protected final def sql(values: I)(implicit read: Read[R], ME: MonadError[F, Throwable]): F[Fragment] = {
    meSql(values, selectEntry, functionName, alias)(read, ME)
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

  def meSql(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    read: Read[StatusWithData[R]],
    ME: MonadError[F, Throwable]
  ): F[Fragment] = {
    ME.catchNonFatal {
      val fragments = toFragmentsSeq(values)
      val args = fragments.toList match {
        case head :: tail => tail.foldLeft(head)((acc, frag) => acc ++ fr"," ++ frag)
        case Nil          => fr""
      }
      sql"SELECT ${Fragment.const(selectEntry)} FROM ${Fragment.const(functionName)}($args) ${Fragment.const(alias)};"
    }
  }

  /**
   *  Generates a `DoobieQueryWithStatus[R]` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the `DoobieQueryWithStatus[R]` representing the SQL query
   */
  protected def query(values: I)(implicit ME: MonadError[F, Throwable]): F[DoobieQueryWithStatus[R]] = {
    queryWithAllInputParams(values, selectEntry, functionName, alias)
  }

  protected def queryWithAllInputParams(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    ME: MonadError[F, Throwable]
  ): F[DoobieQueryWithStatus[R]] = {
    ME.flatMap(meSql(values, selectEntry, functionName, alias))(fr =>
      ME.pure(new DoobieQueryWithStatus[R](fr, checkStatus))
    )
  }

  protected final def sql(values: I)(implicit read: Read[StatusWithData[R]], ME: MonadError[F, Throwable]): F[Fragment] = {
    meSql(values, selectEntry, functionName, alias)(read, ME)
  }

  // This is to be mixed in by an implementation of StatusHandling
  def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A]
}

/**
 *  `DoobieFunction` is an object that contains several abstract classes extending different types of database functions.
 *  These classes use Doobie's `Fragment` to represent SQL queries and `DoobieEngine` to execute them.
 */
object DoobieFunction {

  /**
   *  `DoobieSingleResultFunctionWithStatus` is an abstract class that extends `DBSingleResultFunctionWithStatus` with `DoobiePgEngine` as the engine type.
   *  It represents a database function that returns a single result with status.
   *
   *  @param toFragmentsSeq a function that generates a sequence of `Fragment`s
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobieEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @param readSelectWithStatus the `Read[StatusWithData[R]]` instance used to read the query result with status into `StatusWithData[R]`
   *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
   */
  abstract class DoobieSingleResultFunctionWithStatus[I, R, F[_]: Monad](
    override val toFragmentsSeq: I => Seq[Fragment],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R],
    val readSelectWithStatus: Read[StatusWithData[R]]
  ) extends DBFunctionWithStatus[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunctionWithStatus[I, R, F]

  /**
   *  `DoobieSingleResultFunction` is an abstract class that extends `DBSingleResultFunction` with `DoobiePgEngine` as the engine type.
   *  It represents a database function that returns a single result.
   *
   *  @param toFragmentsSeq a function that generates a sequence of `Fragment`s
   *  @param functionNameOverride the optional override for the function name
   *  @param schema the database schema
   *  @param dbEngine the `DoobieEngine` instance used to execute SQL queries
   *  @param readR the `Read[R]` instance used to read the query result into `R`
   *  @tparam F the effect type, which must have an `Async` and a `Monad` instance
   */
  abstract class DoobieSingleResultFunction[I, R, F[_]: Monad](
    override val toFragmentsSeq: I => Seq[Fragment],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBSingleResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R, F]

  /**
   *  `DoobieMultipleResultFunction` is an abstract class that extends `DBMultipleResultFunction` with `DoobiePgEngine` as the engine type.
   *  It represents a database function that returns multiple results.
   */
  abstract class DoobieMultipleResultFunction[I, R, F[_]: Monad](
    override val toFragmentsSeq: I => Seq[Fragment],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBMultipleResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R, F]

  /**
   *  `DoobieOptionalResultFunction` is an abstract class that extends `DBOptionalResultFunction` with `DoobiePgEngine` as the engine type.
   *  It represents a database function that returns an optional result.
   */
  abstract class DoobieOptionalResultFunction[I, R, F[_]: Monad](
    override val toFragmentsSeq: I => Seq[Fragment],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    val dbEngine: DoobieEngine[F],
    val readR: Read[R]
  ) extends DBOptionalResultFunction[I, R, DoobieEngine[F], F](functionNameOverride)
      with DoobieFunction[I, R, F]
}
