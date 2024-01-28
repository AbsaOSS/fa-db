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

package za.co.absa.fadb.slick

import cats.MonadError
import slick.jdbc.SetParameter
//import cats.implicits.catsStdInstancesForFuture
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.DBFunction.{DBMultipleResultFunction, DBOptionalResultFunction, DBSingleResultFunction}
import za.co.absa.fadb.exceptions.StatusException
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._
import za.co.absa.fadb.{DBFunctionWithStatus, DBSchema, FunctionStatusWithData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.higherKinds

/**
 *  Base class for Slick DB functions.
 *
 *  @tparam I the input type of the function
 *  @tparam R the result type of the function
 */
private[slick] trait SlickFunctionBase[I, R] {

  /**
   *  The `GetResult[R]` instance used to read the query result into `R`.
   */
  protected def slickConverter: GetResult[R]

  def toSQLActionBuilder: I => Seq[SQLActionBuilder]

  /**
   *  Generates a Slick `SQLActionBuilder` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the Slick `SQLActionBuilder` representing the SQL query
   */
//  def sql(values: I, selectEntry: String, functionName: String, alias: String)(implicit
//    ME: MonadError[Future, Throwable]
//  ): Future[SQLActionBuilder] = {
//    ME.catchNonFatal(toSQLActionBuilder(values))
//  }

  def sql(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    ME: MonadError[Future, Throwable]
  ): Future[SQLActionBuilder] = {
    ME.catchNonFatal {
      val fragments = toSQLActionBuilder(values)
      val args = fragments.map(_.queryParts.mkString).mkString(", ") // Concatenate the arguments with commas
      val query = s"SELECT $selectEntry FROM $functionName($args) $alias"
      sql"$query"
    }
  }

  def fieldsToSelect: Seq[String]
}

private[slick] trait SlickFunction[I, R] extends SlickFunctionBase[I, R] {

  /**
   *  Generates a `SlickQuery[R]` representing the SQL query for the function.
   *
   *  @param values the input values for the function
   *  @return the `SlickQuery[R]` representing the SQL query
   */
  protected def query(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    ME: MonadError[Future, Throwable]
  ): Future[SlickQuery[R]] = {
    ME.flatMap(sql(values, selectEntry, functionName, alias))(sth => ME.pure(new SlickQuery[R](sth, slickConverter)))
  }
}

private[slick] trait SlickFunctionWithStatus[I, R] extends SlickFunctionBase[I, R] {

  /**
   *  Generates a `SlickQueryWithStatus[R]` representing the SQL query for the function with status support.
   *
   *  @param values the input values for the function
   *  @return the `SlickQueryWithStatus[R]` representing the SQL query
   */
  protected def query(values: I, selectEntry: String, functionName: String, alias: String)(implicit
    ME: MonadError[Future, Throwable]
  ): Future[SlickQueryWithStatus[R]] = {
    ME.flatMap(sql(values, selectEntry, functionName, alias))(sth =>
      ME.pure(new SlickQueryWithStatus[R](sth, slickConverter, checkStatus))
    )
  }

  // Expected to be mixed in by an implementation of StatusHandling
  def checkStatus[A](statusWithData: FunctionStatusWithData[A]): Either[StatusException, A]
}

object SlickFunction {

  /**
   *  Class for Slick DB functions with status support.
   */
  abstract class SlickSingleResultFunctionWithStatus[I, R](
    override val toSQLActionBuilder: I => Seq[SQLActionBuilder],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    dBEngine: SlickPgEngine
  ) extends DBFunctionWithStatus[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunctionWithStatus[I, R] {

    final def sql(values: I)(implicit ME: MonadError[Future, Throwable]): Future[SQLActionBuilder] = {
      super.sql(values, selectEntry, functionName, alias)
    }
  }

  /**
   *  Class for Slick DB functions with single result.
   */
  abstract class SlickSingleResultFunction[I, R](
    override val toSQLActionBuilder: I => Seq[SQLActionBuilder],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    dBEngine: SlickPgEngine
  ) extends DBSingleResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R] {

    final def sql(values: I)(implicit ME: MonadError[Future, Throwable]): Future[SQLActionBuilder] = {
      super.sql(values, selectEntry, functionName, alias)
    }
  }

  /**
   *  Class for Slick DB functions with multiple results.
   */
  abstract class SlickMultipleResultFunction[I, R](
    override val toSQLActionBuilder: I => Seq[SQLActionBuilder],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    dBEngine: SlickPgEngine
  ) extends DBMultipleResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R] {

    final def sql(values: I)(implicit ME: MonadError[Future, Throwable]): Future[SQLActionBuilder] = {
      super.sql(values, selectEntry, functionName, alias)
    }
  }

  /**
   *  Class for Slick DB functions with optional result.
   */
  abstract class SlickOptionalResultFunction[I, R](
    override val toSQLActionBuilder: I => Seq[SQLActionBuilder],
    functionNameOverride: Option[String] = None
  )(implicit
    override val schema: DBSchema,
    dBEngine: SlickPgEngine
  ) extends DBOptionalResultFunction[I, R, SlickPgEngine, Future](functionNameOverride)
      with SlickFunction[I, R] {

    final def sql(values: I)(implicit ME: MonadError[Future, Throwable]): Future[SQLActionBuilder] = {
      super.sql(values, selectEntry, functionName, alias)
    }
  }
}
