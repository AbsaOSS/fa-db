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


lazy val commonSettings = Seq(
  javacOptions ++= commonJavacOptions,
  scalacOptions ++= commonScalacOptions,
  Test / parallelExecution := false,
  (Compile / compile) := ((Compile / compile) dependsOn printScalaVersion).value, // printScalaVersion is run with compile
  // to mitigate CVE-2022-31183
  dependencyOverrides += "co.fs2" %% "fs2-core" % "3.2.11",
  dependencyOverrides += "co.fs2" %% "fs2-io"   % "3.2.11",
)

lazy val parent = (project in file("."))
  .aggregate(faDbCore, faDBSlick, faDBDoobie)
  .settings(
    name := "root",
    libraryDependencies ++= rootDependencies(scalaVersion.value),
    javacOptions ++= commonJavacOptions,
    scalacOptions ++= commonScalacOptions,
    publish / skip := true,
  )

lazy val faDbCore = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
    libraryDependencies ++= coreDependencies(scalaVersion.value),
  )
  .enablePlugins(JacocoFilterPlugin)

lazy val faDBSlick = (project in file("slick"))
  .settings(commonSettings: _*)
  .settings(
    name := "slick",
    libraryDependencies ++= slickDependencies(scalaVersion.value),
  )
  .dependsOn(faDbCore)
  .enablePlugins(JacocoFilterPlugin)

lazy val faDBDoobie = (project in file("doobie"))
  .settings(commonSettings: _*)
  .settings(
    name := "doobie",
    libraryDependencies ++= doobieDependencies(scalaVersion.value),
  )
  .dependsOn(faDbCore)
  .enablePlugins(JacocoFilterPlugin)

lazy val flywaySettings = project
  .enablePlugins(FlywayPlugin)
  .settings(
    flywayUrl := FlywayConfiguration.flywayUrl,
    flywayUser := FlywayConfiguration.flywayUser,
    flywayPassword := FlywayConfiguration.flywayPassword,
    flywayLocations := FlywayConfiguration.flywayLocations,
    flywaySqlMigrationSuffixes := FlywayConfiguration.flywaySqlMigrationSuffixes,
    libraryDependencies ++= flywayDependencies
  )
addCommandAlias("flywayMigrate", "flywaySettings/flywayMigrate")
