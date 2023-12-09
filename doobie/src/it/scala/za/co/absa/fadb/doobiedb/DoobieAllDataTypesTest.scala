//package za.co.absa.fadb.doobie
//
//import cats.effect.IO
//import cats.effect.unsafe.implicits.global
//import doobie.implicits.toSqlInterpolator
//import doobie.util.Read
//import doobie.util.fragment.Fragment
//import org.scalatest.funsuite.AnyFunSuite
//import za.co.absa.fadb.DBSchema
//import za.co.absa.fadb.doobie.DoobieFunction.DoobieSingleResultFunction
//
//import java.sql.{Date, Time, Timestamp}
//import java.util.UUID
//
//class DoobieAllDataTypesTest extends AnyFunSuite with DoobieTest {
//
//  import doobie.postgres._
//  import doobie.postgres.implicits._
//
//  // https://tpolecat.github.io/doobie/docs/15-Extensions-PostgreSQL.html
//  // https://tpolecat.github.io/doobie/docs/17-FAQ.html#how-do-i-use-java-time-types-with-doobie-
//
//  case class AllDataTypes(
//                           colSmallint: Option[Short],
//                           colInteger: Option[Int],
//                           colBigint: Option[Long],
//                           colDecimal: Option[BigDecimal],
//                           colNumeric: Option[BigDecimal],
//                           colReal: Option[Float],
//                           colDoublePrecision: Option[Double],
//                           colMoney: Option[Double],
//                           colChar: Option[String],
//                           colVarchar: Option[String],
//                           colText: Option[String],
//                           colTimestamp: Option[Timestamp],
//                           colDate: Option[Date],
//                           colTime: Option[Time],
//                           colBoolean: Option[Boolean],
//                           colUuid: Option[UUID],
//                           colJson: Option[String],
//                           colJsonb: Option[String],
//                           colIntArray: Option[Array[Int]],
//                           colTextArray: Option[Array[String]]
//                         )
//
//  implicit val readAllDataTypes: Read[AllDataTypes] = Read[AllDataTypes]
//
//  class GetAllDataTypes(implicit schema: DBSchema, dbEngine: DoobieEngine[IO])
//      extends DoobieSingleResultFunction[Int, AllDataTypes, IO] {
//
//    override def sql(values: Int)(implicit read: Read[AllDataTypes]): Fragment =
//      sql"SELECT * FROM ${Fragment.const(functionName)}($values)"
//  }
//
//  private val getAllDataTypes = new GetAllDataTypes()(Runs, new DoobieEngine(transactor))
//
//  test("DoobieTest") {
//    val result = getAllDataTypes(2).unsafeRunSync()
//    println(result)
//  }
//
//}
