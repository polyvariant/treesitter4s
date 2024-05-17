import java.io.ByteArrayInputStream

ThisBuild / tlBaseVersion := "0.3"
ThisBuild / organization := "org.polyvariant.treesitter4s"
ThisBuild / organizationName := "Polyvariant"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(tlGitHubDev("kubukoz", "Jakub Kozłowski"))
ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / githubWorkflowOSes := Seq("ubuntu-latest", "macos-latest")

ThisBuild / githubWorkflowBuild ~= (WorkflowStep.Run(commands = List("yarn")) +: _)

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(
  x.cross(CrossVersion.full)
)

val compilerPlugins = List(
  crossPlugin("org.polyvariant" % "better-tostring" % "0.3.17")
)

val Scala213 = "2.13.14"
val Scala3 = "3.3.3"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala213, Scala3)

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

def compileTreeSitter(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
  val output = os.Path((config / resourceManaged).value)

  val version = "0.22.6"

  val downloadTo = os.Path(IO.createTemporaryDirectory)

  println(s"Downloading tree-sitter $version to $downloadTo")

  import sys.process._

  requests
    .get(s"https://github.com/tree-sitter/tree-sitter/archive/v$version.tar.gz")
    .readBytesThrough { bytes =>
      val cmd = s"tar -xzf - --directory $downloadTo"

      (cmd #< bytes).!!
    }

  val binaryName = System.mapLibraryName("tree-sitter")
  val binaryPathCompiled = downloadTo / s"tree-sitter-$version" / binaryName

  Process(
    command = List("make", binaryName),
    cwd = Some((downloadTo / s"tree-sitter-$version").toIO),
  ).!!

  val binaryPathGenerated = output / binaryName
  println("writing tree-sitter file")

  os.copy
    .over(
      binaryPathCompiled,
      binaryPathGenerated,
      createFolders = true,
    )

  List(binaryPathGenerated.toIO)
}

def compileBindingsPython(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
  val output = os.Path((config / resourceManaged).value)

  val version = "0.21.0"

  val downloadTo = os.Path(IO.createTemporaryDirectory)

  println(s"Downloading tree-sitter-python $version to $downloadTo")

  import sys.process._

  requests
    .get(s"https://github.com/tree-sitter/tree-sitter-python/archive/v$version.tar.gz")
    .readBytesThrough { bytes =>
      val cmd = s"tar -xzf - --directory $downloadTo"

      (cmd #< bytes).!!
    }

  val binaryName = System.mapLibraryName("tree-sitter-python")
  val binaryPathCompiled = downloadTo / s"tree-sitter-python-$version" / binaryName

  Process(
    command = List("make", binaryName),
    cwd = Some((downloadTo / s"tree-sitter-python-$version").toIO),
  ).!!

  val binaryPathGenerated = output / binaryName
  println("writing tree-sitter-python file")

  os.copy
    .over(
      binaryPathCompiled,
      binaryPathGenerated,
      createFolders = true,
    )

  List(binaryPathGenerated.toIO)
}

lazy val core = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings
  )
  .jvmSettings(
    commonJVMSettings,
    libraryDependencies ++= Seq(
      "net.java.dev.jna" % "jna" % "5.14.0"
    ),
    Compile / resourceGenerators += compileTreeSitter(Compile).taskValue,
  )

lazy val bindingsPython = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "language-python",
    commonSettings,
    Compile / resourceGenerators += compileBindingsPython(Compile).taskValue,
  )
  .dependsOn(core)
  .jvmSettings(commonJVMSettings)

lazy val tests = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings
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
