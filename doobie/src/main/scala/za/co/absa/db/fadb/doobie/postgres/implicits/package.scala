package za.co.absa.db.fadb.doobie.postgres

import cats.Show
import cats.data.NonEmptyList
import doobie.postgres.implicits._
import doobie.{Get, Put}
import io.circe.{Json => CirceJson}
import org.postgresql.jdbc.PgArray
import org.postgresql.util.PGobject
import io.circe.parser._

import scala.util.Try

package object implicits {

  private implicit val showPgArray: Show[PgArray] = Show.fromToString

  implicit val getMapWithOptionStringValues: Get[Map[String, Option[String]]] = Get[Map[String, String]]
    .tmap(map => map.map { case (k, v) => k -> Option(v) })

  private def circeJsonListToPGJsonArrayString(jsonList: List[CirceJson]): String = {
    val arrayElements = jsonList.map { x =>
      // Convert to compact JSON string and escape inner quotes
      val escapedJsonString = x.noSpaces.replace("\"", "\\\"")
      // Wrap in double quotes for the array element
      s""""$escapedJsonString""""
    }

    arrayElements.mkString("{", ",", "}")
  }

  private def pgArrayToListOfCirceJson(pgArray: PgArray): Either[String, List[CirceJson]] = {
    Try {
      Option(pgArray.getArray) match {
        case Some(array: Array[_]) => array.collect {
          case str: String => parse(str).toTry.get
          case other => parse(other.toString).toTry.get
        }.toList
        case None => List.empty[CirceJson]
        case _ => throw new IllegalArgumentException("Unexpected type encountered.")
      }
    }
      .toEither
      .left.map(_.getMessage)
  }

  object Sequence {

    implicit val get: Get[Seq[String]] = Get[List[String]].map(_.toSeq)
    implicit val put: Put[Seq[String]] = Put[List[String]].contramap(_.toList)

  }

  object Json {

    implicit val jsonArrayPut: Put[List[CirceJson]] = {
      Put.Advanced
        .other[PGobject](
          NonEmptyList.of("json[]")
        )
        .tcontramap { a =>
          val o = new PGobject
          o.setType("json[]")
          o.setValue(circeJsonListToPGJsonArrayString(a))
          o
        }
    }

    implicit val jsonArrayGet: Get[List[CirceJson]] = {
      Get.Advanced
        .other[PgArray](
          NonEmptyList.of("json[]")
        )
        .temap(pgArray => pgArrayToListOfCirceJson(pgArray))
    }

  }

  object Jsonb {

    implicit val jsonbArrayPut: Put[List[CirceJson]] = {
      Put.Advanced
        .other[PGobject](
          NonEmptyList.of("jsonb[]")
        )
        .tcontramap { a =>
          val o = new PGobject
          o.setType("jsonb[]")
          o.setValue(circeJsonListToPGJsonArrayString(a))
          o
        }
    }

  }

}
