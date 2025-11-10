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

import sbt.*

object Dependencies {

  private def commonDependencies(scalaVersion: String): Seq[ModuleID] = Seq(
    "org.typelevel"      %% "cats-core" % "2.9.0",
    "org.typelevel"      %% "cats-effect" % "3.5.0",
    "org.scalatest"      %% "scalatest" % "3.1.0"           % Test,
    "org.scalatest"      %% "scalatest-flatspec" % "3.2.0"  % Test,
    "org.scalatestplus"  %% "mockito-1-10" % "3.1.0.0"      % Test
  )

  def rootDependencies(scalaVersion: String): Seq[ModuleID] = Seq()

  def coreDependencies(scalaVersion: String): Seq[ModuleID] = {
    commonDependencies(scalaVersion) ++ Seq(
    )
  }

  def slickDependencies(scalaVersion: String): Seq[ModuleID] = {
    commonDependencies(scalaVersion) ++ Seq(
      "com.typesafe.slick"  %% "slick"                        % "3.3.3",
      "org.slf4j"            % "slf4j-nop"                    % "1.7.26",
      "com.typesafe.slick"  %% "slick-hikaricp"               % "3.3.3",
      "org.postgresql"       % "postgresql"                   % "42.6.0",
      "com.github.tminglei" %% "slick-pg"                     % "0.20.4"   % Optional
    )
  }

  def doobieDependencies(scalaVersion: String): Seq[ModuleID] = {
    commonDependencies(scalaVersion) ++ Seq(
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC11",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC11",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC11",
      "org.tpolecat" %% "doobie-postgres-circe" % "1.0.0-RC11",
      "io.circe" %% "circe-generic" % "0.14.15" % Test
    )
  }




  def flywayDependencies: Seq[ModuleID] = {
    val postgresql = "org.postgresql" % "postgresql" % "42.6.0"

    Seq(postgresql)
  }

}
