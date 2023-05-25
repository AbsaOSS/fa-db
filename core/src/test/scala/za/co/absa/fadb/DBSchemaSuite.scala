/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.fadb

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.naming_conventions.SnakeCaseNaming.Implicits.namingConvention

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DBSchemaSuite extends AnyFunSuite {

  private val awaitTime = Duration("1 sec")
  private val executorThrow = new DBExecutor[String] {
    override def run[R](fnc: QueryFunction[String, R]): Future[Seq[R]] = {
      throw new Exception("Should never get here")
    }
  }

  private val executorResend = new DBExecutor[String] {
    override def run[Int](fnc: QueryFunction[String, Int]): Future[Seq[Int]] = {
      fnc("")
    }
  }

  test("schema name default") {

    class Foo(executor: DBExecutor[String]) extends DBSchema(executor)
    val schema = new Foo(executorThrow)
    assert(schema.schemaName == "foo")
  }

  test("schema name overridden") {
    class Foo(executor: DBExecutor[String]) extends DBSchema(executor, Some("bar"))

    val schema = new Foo(executorThrow)
    assert(schema.schemaName == "bar")
  }

  test("Test run call over") {
    def queryFncSeq(s: String): Future[Seq[Int]] = {
      Future {
        Seq(1, 2, 3)
      }
    }
    def queryFncEmpty(s: String): Future[Seq[Int]] = {
      Future {
        Seq.empty
      }
    }

    class Foo(executor: DBExecutor[String]) extends DBSchema(executor)
    val schema = new Foo(executorResend)

    val resultExecuteSeq = Await.result(schema.execute(queryFncSeq), awaitTime)
    assert(resultExecuteSeq == Seq(1, 2, 3))
    val resultUniqueSeq= Await.result(schema.unique(queryFncSeq), awaitTime)
    assert(resultUniqueSeq == 1)
    val resultOptionSeq = Await.result(schema.option(queryFncSeq), awaitTime)
    assert(resultOptionSeq.contains(1))

    val resultExecuteEmpty = Await.result(schema.execute(queryFncEmpty), awaitTime)
    assert(resultExecuteEmpty.isEmpty)
    val resultOptionEmpty = Await.result(schema.option(queryFncEmpty), awaitTime)
    assert(resultOptionEmpty.isEmpty)

  }

}
