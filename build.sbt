ThisBuild / tlBaseVersion := "0.3"
ThisBuild / organization := "org.polyvariant.treesitter4s"
ThisBuild / organizationName := "Polyvariant"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(tlGitHubDev("kubukoz", "Jakub Kozłowski"))
ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / githubWorkflowOSes := Seq("ubuntu-latest", "macos-latest")

ThisBuild / githubWorkflowBuild ~= (WorkflowStep.Run(commands = List("yarn")) +: _)

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x.cross(CrossVersion.full))

val compilerPlugins = List(
  crossPlugin("org.polyvariant" % "better-tostring" % "0.3.17")
)

val Scala213 = "2.13.14"
val Scala3 = "3.3.3"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala213, Scala3)

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / tlFatalWarnings := false

val commonSettings = Seq(
  libraryDependencies ++= compilerPlugins ++ Seq(
    "com.disneystreaming" %%% "weaver-cats" % "0.8.4" % Test,
    "com.disneystreaming" %%% "weaver-discipline" % "0.8.4" % Test,
    "com.disneystreaming" %%% "weaver-scalacheck" % "0.8.4" % Test,
  )
)

val jvmTargetOptions = Seq("-source", "8", "-target", "8")

val commonJVMSettings = Seq(
  javacOptions ++= jvmTargetOptions,
  doc / javacOptions --= (jvmTargetOptions :+ "-Xlint:all"),
  Test / fork := true,
  scalacOptions ++= {
    if (scalaVersion.value.startsWith("2.13"))
      Seq("-Wnonunit-statement")
    else
      Nil
  },
)

val commonJSSettings = Seq(
  scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings
  )
  .jvmSettings(
    commonJVMSettings,
    libraryDependencies ++= Seq(
      "net.java.dev.jna" % "jna" % "5.14.0"
    ),
  )
  .jsSettings(commonJSSettings)

lazy val bindingsScala = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "language-scala",
    commonSettings,
  )
  .dependsOn(core)
  .jvmSettings(commonJVMSettings)
  .jsSettings(commonJSSettings)

lazy val bindingsPython = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "language-python",
    commonSettings,
  )
  .dependsOn(core)
  .jvmSettings(commonJVMSettings)
  .jsSettings(commonJSSettings)

lazy val tests = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings
  )
  .dependsOn(bindingsScala, bindingsPython)
  .jvmSettings(commonJVMSettings)
  .jsSettings(commonJSSettings)
  .enablePlugins(NoPublishPlugin)

lazy val root = tlCrossRootProject
  .aggregate(core, bindingsScala, bindingsPython, tests)
  .settings(
    Compile / doc / sources := Seq(),
    sonatypeProfileName := "org.polyvariant",
  )
