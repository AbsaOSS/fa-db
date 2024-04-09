package za.co.absa.fadb.tags

import org.scalatest.Tag

object IntegrationTestTag extends Tag("za.co.absa.fadb.IntegrationTestTag")

// run only test with defined tag
// sbt "project faDBSlick" ++2.12.17 "testOnly -- -n za.co.absa.fadb.IntegrationTestTag"

// run all except ones with defined tag
// sbt "project faDBSlick" ++2.12.17 "testOnly -- -l za.co.absa.fadb.IntegrationTestTag"





// run all tests in project - unit (not Tag) and Tagged ones
// sbt ++2.12.17 test

// run all tests in project - excluding the ones with defined tag
// sbt ++2.12.17 "testOnly -- -l za.co.absa.fadb.IntegrationTestTag"

// run all tests in project - limited to the ones with defined tag
// sbt ++2.12.17 "testOnly -- -n za.co.absa.fadb.IntegrationTestTag"

