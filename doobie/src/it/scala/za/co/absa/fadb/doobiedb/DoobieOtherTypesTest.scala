package za.co.absa.fadb.doobiedb

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits.toSqlInterpolator
import doobie.util.Read
import doobie.util.fragment.Fragment
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.fadb.DBSchema
import za.co.absa.fadb.doobiedb.DoobieFunction.DoobieSingleResultFunction

import java.net.InetAddress
import java.util.UUID


class DoobieOtherTypesTest extends AnyFunSuite with DoobieTest {

  import doobie.postgres.implicits._

  case class OtherTypesData(
                             id: Int,
                             ltreeCol: String,
                             inetCol: InetAddress,
                             macaddrCol: String,
                             hstoreCol: Map[String, String],
                             cidrCol: String,
                             jsonCol: String,
                             jsonbCol: String,
                             uuidCol: UUID,
                             arrayCol: Array[Int]
                           )


  class ReadOtherTypes(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
    extends DoobieSingleResultFunction[Int, OtherTypesData, IO] {

    override def sql(values: Int)(implicit read: Read[OtherTypesData]): Fragment =
      sql"SELECT * FROM ${Fragment.const(functionName)}($values)"
  }

  private val readOtherTypes = new ReadOtherTypes()(Runs, new DoobieEngine(transactor))

  test("DoobieTest") {
    val result = readOtherTypes(1).unsafeRunSync()
    println(result)
  }

}
