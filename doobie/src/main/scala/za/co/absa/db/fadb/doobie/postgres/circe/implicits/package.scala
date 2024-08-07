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

package za.co.absa.db.fadb.doobie.postgres.circe

import cats.Show
import cats.data.NonEmptyList
import doobie.{Get, Put}
import io.circe.Json
import org.postgresql.jdbc.PgArray
import org.postgresql.util.PGobject
import io.circe.parser._

import scala.util.{Failure, Success, Try}

package object implicits {

  private implicit val showPgArray: Show[PgArray] = Show.fromToString

  implicit val jsonPut: Put[Json] = doobie.postgres.circe.json.implicits.jsonPut
  implicit val jsonbPut: Put[Json] = doobie.postgres.circe.jsonb.implicits.jsonbPut

  implicit val jsonGet: Get[Json] = doobie.postgres.circe.json.implicits.jsonGet
  implicit val jsonbGet: Get[Json] = doobie.postgres.circe.jsonb.implicits.jsonbGet

  implicit val jsonArrayPut: Put[List[Json]] = {
    Put.Advanced
      .other[PGobject](
        NonEmptyList.of("json[]")
      )
      .tcontramap { a =>
        val o = new PGobject
        o.setType("json[]")
        o.setValue(jsonListToPGJsonArrayString(a))
        o
      }
  }

  implicit val jsonbArrayPut: Put[List[Json]] = {
    Put.Advanced
      .other[PGobject](
        NonEmptyList.of("jsonb[]")
      )
      .tcontramap { a =>
        val o = new PGobject
        o.setType("jsonb[]")
        o.setValue(jsonListToPGJsonArrayString(a))
        o
      }
  }

  // to be used for both json[] and jsonb[] as it handles well both
  // and we want to avoid collision when resolving implicits
  implicit val jsonOrJsonbArrayGet: Get[List[Json]] = {
    Get.Advanced
      .other[PgArray](
        NonEmptyList.of("json[]")
      )
      .temap(pgArray => pgArrayToListOfJson(pgArray))
  }

  private def jsonListToPGJsonArrayString(jsonList: List[Json]): String = {
    val arrayElements = jsonList.map { x =>
      // Convert to compact JSON string and escape inner quotes
      val escapedJsonString = x.noSpaces.replace("\"", "\\\"")
      // Wrap in double quotes for the array element
      s""""$escapedJsonString""""
    }

    arrayElements.mkString("{", ",", "}")
  }

  private def pgArrayToListOfJson(pgArray: PgArray): Either[String, List[Json]] = {
    Try(Option(pgArray.getArray)) match {
      case Success(Some(array: Array[_])) =>
        val results = array.toList.map {
          case str: String => parse(str).left.map(_.getMessage)
          case other => parse(other.toString).left.map(_.getMessage)
        }
        results.partition(_.isLeft) match {
          case (Nil, rights) => Right(rights.collect { case Right(json) => json })
          case (lefts, _) => Left("Failed to parse JSON: " + lefts.collect { case Left(err) => err }.mkString(", "))
        }
      case Success(Some(_)) => Left("Unexpected type encountered. Expected an Array.")
      case Success(None) => Right(Nil)
      case Failure(exception) => Left(exception.getMessage)
    }
  }

}
