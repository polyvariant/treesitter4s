import sbt.util.CacheImplicits._

ThisBuild / tlBaseVersion := "0.3"
ThisBuild / organization := "org.polyvariant.treesitter4s"
ThisBuild / organizationName := "Polyvariant"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(tlGitHubDev("kubukoz", "Jakub Kozłowski"))
ThisBuild / tlSonatypeUseLegacyHost := false
ThisBuild / githubWorkflowOSes := Seq("ubuntu-latest", "macos-latest")

ThisBuild / tlJdkRelease := Some(11)

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

// returns directory we built in
def downloadAndBuild(name: String, version: String, repoUrl: String): os.Path = {
  val binaryName = System.mapLibraryName(name)

  val downloadTo = os.Path(IO.createTemporaryDirectory)

  println(s"Downloading $name $version to $downloadTo")

  import sys.process._

  requests
    .get(s"$repoUrl/archive/v$version.tar.gz")
    .readBytesThrough { bytes =>
      val cmd = s"tar -xzf - --directory $downloadTo"

      (cmd #< bytes).!!
    }

  val extracted = downloadTo / s"$name-$version"

  Process(
    command = List("make", binaryName),
    cwd = Some(extracted.toIO),
  ).!!

  extracted
}

def downloadAndBuildTask(config: Configuration, name: String, version: String, repoUrl: String) =
  Def.task {

    val s = (config / streams).value
    type DownloadArgs = (String, String, String)

    val cached =
      Tracked.inputChanged[DownloadArgs, File](
        s.cacheStoreFactory.make("input")
      ) {
        Function.untupled {
          Tracked.lastOutput[(Boolean, DownloadArgs), File](
            s.cacheStoreFactory.make("output")
          ) { case ((changed, input), lastResult) =>
            lastResult match {
              case Some(cached) if !changed => cached
              case _                        => (downloadAndBuild _).tupled(input).toIO
            }
          }
        }
      }

    cached((name, version, repoUrl))

  }

def copyLibrary(name: String, from: os.Path, to: os.Path): os.Path = {
  val binaryName = System.mapLibraryName(name)

  val source = from / binaryName
  val target = to / binaryName

  os.copy
    .over(
      source,
      target,
      createFolders = true,
    )

  target
}

def compileTreeSitter(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
  val output = os.Path((config / resourceManaged).value)

  val extracted = os.Path(
    downloadAndBuildTask(
      config = config,
      name = "tree-sitter",
      version = "0.22.6",
      repoUrl = "https://github.com/tree-sitter/tree-sitter",
    ).value
  )

  List(copyLibrary("tree-sitter", extracted, output).toIO)
}

def compilePythonGrammar(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
  val output = os.Path((config / resourceManaged).value)

  val extracted = os.Path {
    downloadAndBuildTask(
      config = config,
      name = "tree-sitter-python",
      version = "0.21.0",
      repoUrl = "https://github.com/tree-sitter/tree-sitter-python",
    ).value
  }

  List(copyLibrary("tree-sitter-python", extracted, output).toIO)
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
    Compile / resourceGenerators += compilePythonGrammar(Compile).taskValue,
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
