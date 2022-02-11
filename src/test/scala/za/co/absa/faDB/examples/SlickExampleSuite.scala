package za.co.absa.faDB.examples

import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SlickExampleSuite extends AnyFunSuite {
  test("setup") {
    val se = new SlickExample("pgdb")
    try {
      se.setup()
    } finally se.close()
  }

  test("run") {
    val se = new SlickExample("pgdb")
    try {
      se.read()
    } finally se.close()
  }

  test("query") {
    val se = new SlickExample("pgdb")
    try {
      se.query()
    } finally se.close()
  }

  test("proc") {
    val se = new SlickExample("pgdb")
    try {
      val a = se.coffeesBySuplierUnderPrice(11)
      val b = Await.result(a, Duration.Inf)
      println(b)
    } finally se.close()
  }
}
