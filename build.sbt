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

ThisBuild / name         := "fa-db"
ThisBuild / organization := "za.co.absa"

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.12"

ThisBuild / scalaVersion := scala211
ThisBuild / crossScalaVersions := Seq(scala211, scala212)
ThisBuild / publish := {}

libraryDependencies ++=  List(

  "org.scala-lang"      %  "scala-compiler"   % scalaVersion.value,
  //"org.tpolecat"        %% "skunk-core"       % "0.2.0",
//  "org.scalikejdbc"     %% "scalikejdbc"      % "3.4.+",
//  "com.h2database"      %  "h2"               % "1.4.+",
//  "ch.qos.logback"      %  "logback-classic"  % "1.2.+",

  "com.typesafe.slick"  %% "slick"            % "3.3.3",
  "org.slf4j"            % "slf4j-nop"        % "1.6.4",
  "com.typesafe.slick"  %% "slick-hikaricp"   % "3.3.3",
//  "org.postgresql"       % "postgresql"       % "9.4-1206-jdbc42",
//
//  "io.github.finagle"   %% "finagle-postgres" % "0.13.0",

  "org.scalatest"       %% "scalatest"        % "3.2.9"  % Test,
  "org.scalamock"       %% "scalamock"        % "5.1.0"  % Test
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value
