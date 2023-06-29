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

import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits._
import za.co.absa.fadb.slick.FaDbPostgresProfile.api._
import org.scalatest.funsuite.AnyFunSuite
import slick.jdbc.{GetResult, SQLActionBuilder}
import za.co.absa.fadb.DBFunction.DBSingleResultFunction
import za.co.absa.fadb.DBSchema
import com.github.tminglei.slickpg.{InetString, LTree, MacAddrString, Range}

import java.time.{Duration, LocalDate, LocalDateTime, LocalTime, OffsetDateTime, ZonedDateTime}
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.{FiniteDuration, Duration => ScalaDuration}
import scala.language.implicitConversions
import scala.concurrent.ExecutionContext.Implicits.global

class FaDbPostgresProfileSuite extends AnyFunSuite {

  implicit def javaDurationToScalaDuration(duration: Duration): ScalaDuration = {
    FiniteDuration(duration.toNanos, TimeUnit.NANOSECONDS)
  }
  private val database = Database.forConfig("postgrestestdb")
  private val testDBEngine: SlickPgEngine = new SlickPgEngine(database)


  test("Test query types support with actual values") {

    case class InputOutput(
                    uuid1:      UUID,               //uuid
                    dateTime1:  LocalDate,          //date
                    dateTime2:  LocalTime,          //time
                    dateTime3:  LocalDateTime,      //timestamp
                    dateTime4:  Duration,           //interval
                    dateTime5:  ZonedDateTime,      //timestamptz
                    dateTime6:  OffsetDateTime,     //timestamptz
                    range1:     Range[Int],         //range
                    lTree1:     LTree,              //ltree
                    map1:       Map[String, String],//hstore
                    inet1:      InetString,         //inet
                    macaddr1:   MacAddrString       //macaddr
                    )

    class TestFunction(implicit override val schema: DBSchema, override val dbEngine: SlickPgEngine)
      extends DBSingleResultFunction[InputOutput, InputOutput, SlickPgEngine]
        with SlickFunction[InputOutput, InputOutput] {

      override protected def sql(values: InputOutput): SQLActionBuilder = {
        sql"""SELECT #$selectEntry
            FROM #$functionName(
              ${values.uuid1},
              ${values.dateTime1},
              ${values.dateTime2},
              ${values.dateTime3},
              ${values.dateTime4},
              ${values.dateTime5},
              ${values.dateTime6},
              ${values.range1},
              ${values.lTree1},
              ${values.map1},
              ${values.inet1},
              ${values.macaddr1}
            ) #$alias;"""
      }

      override protected def slickConverter: GetResult[InputOutput] = GetResult{r => InputOutput(
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<
      )}
    }

    class TestSchema (implicit dBEngine: SlickPgEngine) extends DBSchema("public"){

      val testFunction = new TestFunction
    }


    val input = InputOutput(
      UUID.randomUUID(),
      LocalDate.now(),
      LocalTime.now(),
      LocalDateTime.now(),
      Duration.ofMinutes(42),
      ZonedDateTime.now(),
      OffsetDateTime.now(),
      range1 = Range(7, 13),
      LTree(List("This", "is", "an", "LTree")),
      Map("a" -> "Hello", "bb" -> "beautiful", "ccc" -> "world"),
      InetString("168.0.0.1"),
      MacAddrString("12:34:56:78:90:ab")
    )
    // because postgres does not fully support time zone as Java, so we need to clear it for later successful assert
    val expected = input.copy(dateTime5 = input.dateTime5.toOffsetDateTime.toZonedDateTime)


    val timeout = Duration.ofMinutes(1)
    val result = Await.result(new TestSchema()(testDBEngine).testFunction(input), timeout)
    assert(result == expected)
  }

  test("Test query types support with NULL values") {

    case class InputOutput(
                            uuid1:      Option[UUID],                 //uuid
                            dateTime1:  Option[LocalDate],            //date
                            dateTime2:  Option[LocalTime],            //time
                            dateTime3:  Option[LocalDateTime],        //timestamp
                            dateTime4:  Option[Duration],             //interval
                            dateTime5:  Option[ZonedDateTime],        //timestamptz
                            dateTime6:  Option[OffsetDateTime],       //timestamptz
                            range1:     Option[Range[Int]],           //range
                            lTree1:     Option[LTree],                //ltree
                            map1:       Option[Map[String, String]],  //hstore
                            inet1:      Option[InetString],           //inet
                            macaddr1:   Option[MacAddrString]          //macaddr
                          )

    class TestFunction(implicit override val schema: DBSchema, override val dbEngine: SlickPgEngine)
      extends DBSingleResultFunction[InputOutput, InputOutput, SlickPgEngine]
        with SlickFunction[InputOutput, InputOutput] {

      override protected def sql(values: InputOutput): SQLActionBuilder = {
        sql"""SELECT #$selectEntry
            FROM #$functionName(
              ${values.uuid1},
              ${values.dateTime1},
              ${values.dateTime2},
              ${values.dateTime3},
              ${values.dateTime4},
              ${values.dateTime5},
              ${values.dateTime6},
              ${values.range1},
              ${values.lTree1},
              ${values.map1},
              ${values.inet1},
              ${values.macaddr1}
            ) #$alias;"""
      }

      override protected def slickConverter: GetResult[InputOutput] = GetResult{r => InputOutput(
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.<<,
        r.nextHStoreOption,
        r.<<,
        r.nextMacAddrOption
      )}
    }

    class TestSchema (implicit dBEngine: SlickPgEngine) extends DBSchema("public"){

      val testFunction = new TestFunction
    }


    val inputOutput = InputOutput(
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None
    )

    val timeout = Duration.ofMinutes(1)
    val result = Await.result(new TestSchema()(testDBEngine).testFunction(inputOutput), timeout)
    assert(result == inputOutput)
  }

}

