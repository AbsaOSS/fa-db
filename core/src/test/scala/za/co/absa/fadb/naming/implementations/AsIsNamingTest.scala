package za.co.absa.fadb.naming.implementations

import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import za.co.absa.fadb.naming.LettersCase

class AsIsNamingTest extends AnyFunSuiteLike with Matchers {

  val asIsNaming = new AsIsNaming(LettersCase.AsIs)

  test("AsIsNaming should return the same string") {
    val input = "testString"
    val expectedOutput = "testString"

    val output = asIsNaming.stringPerConvention(input)

    output shouldEqual expectedOutput
  }

}
