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

import sbt._

object Dependencies {


  private def coomonDependencies(scalaVersion: String): Seq[ModuleID] = Seq(
    "org.scala-lang"      %  "scala-compiler"               % scalaVersion,
    "org.scalatest"      %% "scalatest" % "3.1.0"           % Test,
    "org.scalatest"      %% "scalatest-flatspec" % "3.2.0"  % Test,
    "org.scalatestplus"  %% "mockito-1-10" % "3.1.0.0"      % Test
  )

  def rootDependencies(scalaVersion: String): Seq[ModuleID] = Seq()

  def coreDependencies(scalaVersion: String): Seq[ModuleID] = {
    coomonDependencies(scalaVersion) ++ Seq(
    )
  }

  def slickDependencies(scalaVersion: String): Seq[ModuleID] = {
    coomonDependencies(scalaVersion) ++ Seq(
      "com.typesafe.slick"  %% "slick"                        % "3.3.3",
      "org.slf4j"            % "slf4j-nop"                    % "1.6.4",
      "com.typesafe.slick"  %% "slick-hikaricp"               % "3.3.3",
      "org.postgresql"       % "postgresql"                   % "9.4-1206-jdbc42",
    )
  }

  def examplesDependencies(scalaVersion: String): Seq[ModuleID] = {
    slickDependencies(scalaVersion)
  }
}
