/*
 * Copyright 2021 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.faDB.examples

//#imports
// Use H2Profile to connect to an H2 database
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
//#imports
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.collection.mutable.ArrayBuffer

/**
  * A simple example that uses statically typed queries against an in-memory
  * H2 database. The example data comes from Oracle's JDBC tutorial at
  * http://docs.oracle.com/javase/tutorial/jdbc/basics/tables.html.
  */
class FirstSlickExample(val foo: String)  {
  val lines = new ArrayBuffer[Any]()
  //def println(s: Any) = lines += s

  //#tables
  // Definition of the SUPPLIERS table

  def run(): Unit = {
    class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
      def id = column[Int]("SUP_ID", O.PrimaryKey) // This is the primary key column
      def name = column[String]("SUP_NAME")
      def street = column[String]("STREET")
      def city = column[String]("CITY")
      def state = column[String]("STATE")
      def zip = column[String]("ZIP")
      // Every table needs a * projection with the same type as the table's type parameter
      def * = (id, name, street, city, state, zip)
    }
    val suppliers = TableQuery[Suppliers]

    // Definition of the COFFEES table
    class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
      def name = column[String]("COF_NAME", O.PrimaryKey)
      def supID = column[Int]("SUP_ID")
      def price = column[Double]("PRICE")
      def sales = column[Int]("SALES")
      def total = column[Int]("TOTAL")
      def * = (name, supID, price, sales, total)
      // A reified foreign key relation that can be navigated to create a join
      def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
    }
    val coffees = TableQuery[Coffees]
    //#tables

    // Connect to the database and execute the following block within a session
    //#setup
    val db = Database.forConfig("pgdb")
    try {
      // val resultFuture: Future[_] = { ... }
      //#setup

      //#create
      val setup = DBIO.seq(
        // Create the tables, including primary and foreign keys
        (suppliers.schema ++ coffees.schema).create,

        // Insert some suppliers
        suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
        suppliers += (49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
        suppliers += (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966"),
        // Equivalent SQL code:
        // insert into SUPPLIERS(SUP_ID, SUP_NAME, STREET, CITY, STATE, ZIP) values (?,?,?,?,?,?)

        // Insert some coffees (using JDBC's batch insert feature, if supported by the DB)
        coffees ++= Seq(
          ("Colombian", 101, 7.99, 0, 0),
          ("French_Roast", 49, 8.99, 0, 0),
          ("Espresso", 150, 9.99, 0, 0),
          ("Colombian_Decaf", 101, 8.99, 0, 0),
          ("French_Roast_Decaf", 49, 9.99, 0, 0)
        )
      )
        // Equivalent SQL code:
        // insert into COFFEES(COF_NAME, SUP_ID, PRICE, SALES, TOTAL) values (?,?,?,?,?)

      val setupFuture = db.run(setup)

    // Connect to the database and execute the following block within a session
    //#setup
      val resultFuture = setupFuture.flatMap { _ =>

        //#readall
        // Read all coffees and print them to the console
        println("Coffees:")
        db.run(coffees.result).map(_.foreach {
          case (name, supID, price, sales, total) =>
            println("  " + name + "\t" + supID + "\t" + price + "\t" + sales + "\t" + total)
        })
        // Equivalent SQL code:
        // select COF_NAME, SUP_ID, PRICE, SALES, TOTAL from COFFEES
        //#readall

      }.flatMap { _ =>

        //#projection
        // Why not let the database do the string conversion and concatenation?
        //#projection
        println("Coffees (concatenated by DB):")
        //#projection
        val q1 = for (c <- coffees)
          yield LiteralColumn("  ") ++ c.name ++ "\t" ++ c.supID.asColumnOf[String] ++
            "\t" ++ c.price.asColumnOf[String] ++ "\t" ++ c.sales.asColumnOf[String] ++
            "\t" ++ c.total.asColumnOf[String]
        // The first string constant needs to be lifted manually to a LiteralColumn
        // so that the proper ++ operator is found

        // Equivalent SQL code:
        // select '  ' || COF_NAME || '\t' || SUP_ID || '\t' || PRICE || '\t' SALES || '\t' TOTAL from COFFEES

        db.stream(q1.result).foreach(println)
        //#projection

      }.flatMap { _ =>

        //#join
        // Perform a join to retrieve coffee names and supplier names for
        // all coffees costing less than $9.00
        //#join
        println("Manual join:")
        //#join
        val q2 = for {
          c <- coffees if c.price < 9.0
          s <- suppliers if s.id === c.supID
        } yield (c.name, s.name)
        // Equivalent SQL code:
        // select c.COF_NAME, s.SUP_NAME from COFFEES c, SUPPLIERS s where c.PRICE < 9.0 and s.SUP_ID = c.SUP_ID
        //#join
        db.run(q2.result).map(_.foreach(t =>
          println("  " + t._1 + " supplied by " + t._2)
        ))

      }.flatMap { _ =>

        // Do the same thing using the navigable foreign key
        println("Join by foreign key:")
        //#fkjoin
        val q3 = for {
          c <- coffees if c.price < 9.0
          s <- c.supplier
        } yield (c.name, s.name)
        // Equivalent SQL code:
        // select c.COF_NAME, s.SUP_NAME from COFFEES c, SUPPLIERS s where c.PRICE < 9.0 and s.SUP_ID = c.SUP_ID
        //#fkjoin

        db.run(q3.result).map(_.foreach { case (s1, s2) => println("  " + s1 + " supplied by " + s2) })

      }
      //#setup
      Await.result(resultFuture, Duration.Inf)
    } finally db.close
  }
  //#setup
}