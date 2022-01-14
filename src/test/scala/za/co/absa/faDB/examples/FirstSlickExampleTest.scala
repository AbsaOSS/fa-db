package za.co.absa.faDB.examples

import org.scalatest.funsuite.AnyFunSuite

class FirstSlickExampleTest extends AnyFunSuite {
  test("setup") {
    val fse = new FirstSlickExample("bar")
    println("Hello")
    fse.run()
    println(fse.foo)
  }
}
