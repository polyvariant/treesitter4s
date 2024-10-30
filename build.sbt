import sjsonnew.BasicJsonProtocol

import sjsonnew.JsonFormat

import sbt.util.CacheImplicits._

ThisBuild / tlBaseVersion := "0.3"
ThisBuild / organization := "org.polyvariant.treesitter4s"
ThisBuild / organizationName := "Polyvariant"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(tlGitHubDev("kubukoz", "Jakub Kozłowski"))
ThisBuild / sonatypeCredentialHost := Sonatype.sonatype01
ThisBuild / githubWorkflowOSes := Seq(
  "ubuntu-latest", // x86
  "macos-latest", // arm64
  "macos-13", // x64
)

ThisBuild / tlJdkRelease := Some(11)
ThisBuild / tlCiDependencyGraphJob := false

ThisBuild / githubWorkflowBuild ~= (WorkflowStep.Run(commands = List("yarn")) +: _)

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(
  x.cross(CrossVersion.full)
)

val compilerPlugins = List(
  crossPlugin("org.polyvariant" % "better-tostring" % "0.3.17")
)

val Scala212 = "2.12.20"
val Scala213 = "2.13.15"
val Scala3 = "3.3.4"

ThisBuild / scalaVersion := Scala3
ThisBuild / crossScalaVersions := Seq(Scala3)

ThisBuild / tlFatalWarnings := false

val commonSettings = Seq(
  libraryDependencies ++= compilerPlugins ++ Seq(
    "com.disneystreaming" %%% "weaver-cats" % "0.8.4" % Test,
    "com.disneystreaming" %%% "weaver-discipline" % "0.8.4" % Test,
    "com.disneystreaming" %%% "weaver-scalacheck" % "0.8.4" % Test,
  )
)

val commonJVMSettings = Seq(
  Compile / doc / javacOptions -= "-Xlint:all",
  Test / fork := true,
  scalacOptions ++= {
    if (scalaVersion.value.startsWith("2.13"))
      Seq("-Wnonunit-statement")
    else
      Nil
  },
)

lazy val core = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings
  )
  .jvmSettings(
    commonJVMSettings,
    libraryDependencies ++= Seq(
      "net.java.dev.jna" % "jna" % "5.14.0",
      "org.javassist" % "javassist" % "3.28.0-GA",
    ),
  )

lazy val bindingsPython = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "language-python",
    commonSettings,
  )
  .dependsOn(core)
  .jvmSettings(commonJVMSettings)

lazy val tests = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings,
    run / fork := true,
    javaOptions ++= Seq(
      "--add-opens",
      "java.base/java.lang=ALL-UNNAMED",
    ),
  )
  .dependsOn(bindingsPython)
  .jvmSettings(commonJVMSettings)
  .enablePlugins(NoPublishPlugin)

lazy val root = tlCrossRootProject
  .aggregate(core, bindingsPython, tests)
  .settings(
    Compile / doc / sources := Seq(),
    sonatypeProfileName := "org.polyvariant",
  )
