package za.co.absa.fadb.slick

import slick.jdbc.JdbcBackend.Database
import za.co.absa.fadb.DBSchema

trait SlickTest {
  case class GetActorsQueryParameters(firstName: Option[String], lastName: Option[String])

  import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits._
  object Runs extends DBSchema

  val db = Database.forConfig("postgrestestdb")
}
