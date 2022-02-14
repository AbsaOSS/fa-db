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

ThisBuild / organization := "za.co.absa"

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.12"

ThisBuild / scalaVersion := scala211
ThisBuild / crossScalaVersions := Seq(scala211, scala212)

import Dependencies._


lazy val printScalaVersion = taskKey[Unit]("Print Scala versions faDB is being built for.")

ThisBuild / printScalaVersion := {
  val log = streams.value.log
  log.info(s"Building with Scala ${scalaVersion.value}")
}

lazy val parent = (project in file("."))
  .aggregate(faDbCore, faDBSlick, faDBExamples)
  .settings(
    name := "fa-db",
    libraryDependencies ++= rootDependencies(scalaVersion.value),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    publish / skip := true
  )

lazy val faDbCore = (project in file("core"))
  .settings(
    name := "fa-db-core",
    libraryDependencies ++= coreDependencies(scalaVersion.value),
    (Compile / compile) := ((Compile / compile) dependsOn printScalaVersion).value // printScalaVersion is run with compile
  )

lazy val faDBSlick = (project in file("slick"))
  .settings(
    name := "fa-db-slick",
    libraryDependencies ++= slickDependencies(scalaVersion.value),
    Test / parallelExecution := false,
    (Compile / compile) := ((Compile / compile) dependsOn printScalaVersion).value // printScalaVersion is run with compile
  ).dependsOn(faDbCore)

lazy val faDBExamples = (project in file("examples"))
  .settings(
    name := "fa-db-examples",
    libraryDependencies ++= examplesDependencies(scalaVersion.value),
    Test / parallelExecution := false,
    (Compile / compile) := ((Compile / compile) dependsOn printScalaVersion).value, // printScalaVersion is run with compile
    publish / skip := true
  ).dependsOn(faDbCore, faDBSlick)

releasePublishArtifactsAction := PgpKeys.publishSigned.value
