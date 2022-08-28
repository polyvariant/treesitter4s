ThisBuild / tlBaseVersion := "0.1"
ThisBuild / organization := "org.polyvariant.treesitter4s"
ThisBuild / organizationName := "Polyvariant"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(tlGitHubDev("kubukoz", "Jakub Kozłowski"))
ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / githubWorkflowOSes := Seq("ubuntu-latest", "macos-latest")

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x.cross(CrossVersion.full))

val compilerPlugins = List(
  crossPlugin("org.polyvariant" % "better-tostring" % "0.3.16")
)

val Scala213 = "2.13.8"
val Scala3 = "3.1.3"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala213, Scala3)

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / tlFatalWarnings := false
ThisBuild / tlFatalWarningsInCi := false

val commonSettings = Seq(
  libraryDependencies ++= compilerPlugins ++ Seq(
    "com.disneystreaming" %%% "weaver-cats" % "0.7.15" % Test,
    "com.disneystreaming" %%% "weaver-discipline" % "0.7.15" % Test,
    "com.disneystreaming" %%% "weaver-scalacheck" % "0.7.15" % Test,
  ),
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
)

val commonJVMSettings = Seq(
  javacOptions ++= Seq("-source", "8", "-target", "8")
)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings
  )
  .jvmSettings(commonJVMSettings)

lazy val bindings = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "net.java.dev.jna" % "jna" % "5.12.1"
    ),
  )
  .dependsOn(core)
  .jvmSettings(commonJVMSettings)

lazy val root = tlCrossRootProject
  .aggregate(core, bindings)
  .settings(
    Compile / doc / sources := Seq(),
    sonatypeProfileName := "org.polyvariant",
  )
