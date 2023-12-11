package za.co.absa.fadb.naming

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.fadb.exceptions.NamingException

class ExplicitNamingRequiredTest extends AnyFunSuiteLike {

  test("ExplicitNamingRequired throws NamingException for any string") {
    val namingConvention = new ExplicitNamingRequired()

    val testStrings = Seq("test", "anotherTest", "123", "!@#$%^&*()")

    testStrings.foreach { testString =>
      assertThrows[NamingException] {
        namingConvention.stringPerConvention(testString)
      }
    }
  }

  test("Implicit NamingConvention throws NamingException for any string") {
    import ExplicitNamingRequired.Implicits._

    val testStrings = Seq("test", "anotherTest", "123", "!@#$%^&*()")

    testStrings.foreach { testString =>
      assertThrows[NamingException] {
        namingConvention.stringPerConvention(testString)
      }
    }
  }

}
