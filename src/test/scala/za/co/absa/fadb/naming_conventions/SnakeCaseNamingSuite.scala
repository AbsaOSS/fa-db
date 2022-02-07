package za.co.absa.fadb.naming_conventions

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.fadb.naming_conventions.lettersCase.LettersCase._

class SnakeCaseNamingSuite extends AnyWordSpec with Matchers {
  private class ThisIsATestClass
  private val testInstance = new ThisIsATestClass()


  "stringPerConvention" should {
    "handle empty string" in {
      val nm = new SnakeCaseNaming(AsIs)
      nm.stringPerConvention("") should be("")
    }
  }

  "fromClassNamePerConvention" should {
    "return snake case" when {
      "requested as is" in {
        val nm = new SnakeCaseNaming(AsIs)
        val result = nm.fromClassNamePerConvention(testInstance)
        result should be("This_Is_A_Test_Class")
      }
      "requested as lowercase" in {
        val nm = new SnakeCaseNaming(LowerCase)
        val result = nm.fromClassNamePerConvention(testInstance)
        result should be("this_is_a_test_class")
      }
      "requested as upper case" in {
        val nm = new SnakeCaseNaming(UpperCase)
        val result = nm.fromClassNamePerConvention(testInstance)
        result should be("THIS_IS_A_TEST_CLASS")
      }
    }
  }
}
