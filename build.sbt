/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Dependencies._
import com.github.sbt.jacoco.report.JacocoReportSettings

ThisBuild / organization := "za.co.absa.fa-db"

lazy val scala212 = "2.12.17"
lazy val scala213 = "2.13.12"

ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Seq(scala212, scala213)

ThisBuild / versionScheme := Some("early-semver")

lazy val printScalaVersion = taskKey[Unit]("Print Scala versions faDB is being built for.")
lazy val commonJavacOptions = Seq("-source", "1.8", "-target", "1.8", "-Xlint")
lazy val commonScalacOptions = Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

ThisBuild / printScalaVersion := {
  val log = streams.value.log
  log.info(s"Building with Scala ${scalaVersion.value}")
  log.info(s"Local maven ${Resolver.mavenLocal}")
}

lazy val commonJacocoReportSettings: JacocoReportSettings = JacocoReportSettings(
  formats = Seq(JacocoReportFormats.HTML, JacocoReportFormats.XML)
)

/**
  * add `za.co.absa.fadb.naming.NamingConvention`  to filter a class
  * or  `za.co.absa.fadb.naming.NamingConvention*` to filter the class and all related objects
  */
lazy val commonJacocoExcludes: Seq[String] = Seq(
)

enablePlugins(FlywayPlugin)
flywayUrl := FlywayConfiguration.flywayUrl
flywayUser := FlywayConfiguration.flywayUser
flywayPassword := FlywayConfiguration.flywayPassword
flywayLocations := FlywayConfiguration.flywayLocations
flywaySqlMigrationSuffixes := FlywayConfiguration.flywaySqlMigrationSuffixes
libraryDependencies ++= flywayDependencies

lazy val parent = (project in file("."))
  .aggregate(faDbCore, faDBSlick, faDBDoobie, faDBExamples)
  .settings(
    name := "root",
    libraryDependencies ++= rootDependencies(scalaVersion.value),
    javacOptions ++= commonJavacOptions,
    scalacOptions ++= commonScalacOptions,
    publish / skip := true,
    Defaults.itSettings
  )

lazy val faDbCore = (project in file("core"))
  .configs(IntegrationTest)
  .settings(
    name := "core",
    libraryDependencies ++= coreDependencies(scalaVersion.value),
    javacOptions ++= commonJavacOptions,
    scalacOptions ++= commonScalacOptions,
    (Compile / compile) := ((Compile / compile) dependsOn printScalaVersion).value, // printScalaVersion is run with compile
  )
  .settings(
    jacocoReportSettings := commonJacocoReportSettings.withTitle(s"fa-db:core Jacoco Report - scala:${scalaVersion.value}"),
    jacocoExcludes := commonJacocoExcludes
  )

lazy val faDBSlick = (project in file("slick"))
  .configs(IntegrationTest)
  .settings(
    name := "slick",
    libraryDependencies ++= slickDependencies(scalaVersion.value),
    javacOptions ++= commonJavacOptions,
    scalacOptions ++= commonScalacOptions,
    (Compile / compile) := ((Compile / compile) dependsOn printScalaVersion).value, // printScalaVersion is run with compile
    Defaults.itSettings,
  ).dependsOn(faDbCore)
  .settings(
    jacocoReportSettings := commonJacocoReportSettings.withTitle(s"fa-db:slick Jacoco Report - scala:${scalaVersion.value}"),
    jacocoExcludes := commonJacocoExcludes
  )

lazy val faDBDoobie = (project in file("doobie"))
  .configs(IntegrationTest)
  .settings(
    name := "doobie",
    libraryDependencies ++= doobieDependencies(scalaVersion.value),
    javacOptions ++= commonJavacOptions,
    scalacOptions ++= commonScalacOptions,
    Defaults.itSettings,
  ).dependsOn(faDbCore)
  .settings(
    jacocoReportSettings := commonJacocoReportSettings.withTitle(s"fa-db:doobie Jacoco Report - scala:${scalaVersion.value}"),
    jacocoExcludes := commonJacocoExcludes
  )

// Commented version is able to run unit tests with Int. test and measure coverage
//lazy val jacocoITReport = taskKey[Unit]("Generates JaCoCo report for IT tests in faDBDoobie")
//
//lazy val faDBDoobie = (project in file("doobie"))
//  .enablePlugins(JacocoPlugin)
//  .configs(IntegrationTest)
//  .settings(
//    name := "doobie",
//    libraryDependencies ++= doobieDependencies(scalaVersion.value),
//    javacOptions ++= commonJavacOptions,
//    scalacOptions ++= commonScalacOptions,
//    Defaults.itSettings, // Apply default settings for integration tests
//    // IntegrationTest-specific settings
//    IntegrationTest / fork := true,
//    IntegrationTest / parallelExecution := false,
//    IntegrationTest / javaOptions ++= Seq(
//      "-javaagent:/Users/ab024ll/.m2/repository/org/jacoco/org.jacoco.agent/0.8.10/org.jacoco.agent-0.8.10-runtime.jar=destfile=target/scala-2.12/jacoco/data/jacoco.exec,includes=*,append=true"
//    ),
//    // Define the custom task within the IntegrationTest configuration
//    jacocoITReport := (Def.taskDyn {
//      (IntegrationTest / test).value
//      Def.task { (Test / jacocoReport).value }
//    }).value,
//    jacocoReportSettings := commonJacocoReportSettings.withTitle(s"fa-db:doobie Jacoco Report - scala:${scalaVersion.value}"),
//    jacocoExcludes := commonJacocoExcludes,
//  ).dependsOn(faDbCore)

lazy val faDBExamples = (project in file("examples"))
  .configs(IntegrationTest)
  .settings(
    name := "examples",
    libraryDependencies ++= examplesDependencies(scalaVersion.value),
    Test / parallelExecution := false,
    (Compile / compile) := ((Compile / compile) dependsOn printScalaVersion).value, // printScalaVersion is run with compile
    publish / skip := true
  ).dependsOn(faDbCore, faDBSlick)
  .settings(
    jacocoReportSettings := commonJacocoReportSettings.withTitle(s"fa-db:examples Jacoco Report - scala:${scalaVersion.value}"),
    jacocoExcludes := commonJacocoExcludes
  )

sonatypeProfileName := "za.co.absa"
