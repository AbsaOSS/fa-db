package za.co.absa.fadb

import org.scalatest.funsuite.AnyFunSuiteLike

import za.co.absa.fadb.naming.implementations.SnakeCaseNaming.Implicits._

class DBFunctionFabricUnitTests extends AnyFunSuiteLike {

  implicit object TestSchema extends DBSchema

  test("DBFunctionFabric should use provided function name") {
    val fabric = new DBFunctionFabric(Some("testFunction")) {}
    assert(fabric.functionName == "test_schema.testFunction")
  }

  test("DBFunctionFabric should generate function name from class name if not provided") {
    case class TestFunction() extends DBFunctionFabric(None) {}
    val function = TestFunction()
    assert(function.functionName == "test_schema.test_function")
  }

  test("DBFunctionFabric should generate function name without schema if schema name is empty") {
    implicit object TestSchema extends DBSchema("")

    val fabric = new DBFunctionFabric(Some("testFunction")) {}
    assert(fabric.functionName == "testFunction")
  }

  test("DBFunctionFabric should return empty sequence for fieldsToSelect by default") {
    val fabric = new DBFunctionFabric(Some("testFunction")) {}
    assert(fabric.fieldsToSelect.isEmpty)
  }

  test("DBFunctionFabric should generate selectEntry correctly") {
    val fabric = new DBFunctionFabric(Some("testFunction")) {
      override def fieldsToSelect: Seq[String] = Seq("field1", "field2")
    }
    assert(fabric.selectEntry == "FNC.field1,FNC.field2")
  }

  test("DBFunctionFabric should generate selectEntry as * if fieldsToSelect is empty") {
    val fabric = new DBFunctionFabric(Some("testFunction")) {}
    assert(fabric.selectEntry == "*")
  }

}
