package za.co.absa.faDB.namingConventions

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.faDB.exceptions.NamingException

class ExplicitNamingRequiredSuite extends AnyWordSpec with Matchers {
  private val explicitNamingRequired = new ExplicitNamingRequired()

  "stringPerConvention" should {
    "fail" in {
      intercept[NamingException] {
        explicitNamingRequired.stringPerConvention("")
      }
    }
  }

  "fromClassNamePerConvention" should {
    "fail" in {
      intercept[NamingException] {
        explicitNamingRequired.fromClassNamePerConvention(this)
      }
    }
  }
}
