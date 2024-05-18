/*
 * Copyright 2022 Polyvariant
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

package org.polyvariant.treesitter4s.sbt

import sbt._

import sjsonnew.JsonFormat
import sbt.Keys._
import sjsonnew.BasicJsonProtocol

import sbt.util.CacheImplicits._

object TreeSitter4sPlugin extends AutoPlugin {

  object autoImport {

    val ts4sCompileCore = settingKey[Boolean](
      "Whether to build the core tree-sitter library."
    )

    val ts4sTreeSitterVersion = settingKey[String]("Version of tree-sitter to compile.")

    val ts4sGrammars = settingKey[Seq[TreeSitterGrammar]]("Grammars to compile binaries for.")

    val ts4sBuildCore = taskKey[Seq[File]]("Build the core tree-sitter library.")
    val ts4sBuildGrammars = taskKey[Seq[File]]("Build the tree-sitter grammars.")

    case class TreeSitterGrammar(
      language: String,
      version: String,
    )

  }

  import autoImport._

  private object internals {

    // returns path to binary
    def downloadAndBuild(lib: Library): os.Path = {
      val name = lib.name
      val version = lib.version
      val binaryName = System.mapLibraryName(lib.name)

      val downloadTo = os.Path(IO.createTemporaryDirectory)

      println(s"Downloading $name $version to $downloadTo")

      import sys.process._

      requests
        .get(s"${lib.repoUrl}/archive/v$version.tar.gz")
        .readBytesThrough { bytes =>
          val cmd = s"tar -xzf - --directory $downloadTo"

          (cmd #< bytes).!!
        }

      val extracted = downloadTo / s"$name-$version"

      Process(
        command = List("make", binaryName),
        cwd = Some(extracted.toIO),
      ).!!

      extracted / binaryName
    }

    def simplyCached[Input: JsonFormat, Output: JsonFormat](
      f: Input => Output
    )(
      s: TaskStreams,
      tag: String,
    ): Input => Output = {
      val factory = s.cacheStoreFactory.sub(tag)

      Tracked.inputChanged[Input, Output](
        factory.make("input")
      ) {
        Function.untupled {
          Tracked.lastOutput[(Boolean, Input), Output](
            factory.make("output")
          ) { case ((changed, input), lastResult) =>
            lastResult match {
              case Some(cached) if !changed => cached
              case _                        => f(input)
            }
          }
        }
      }
    }

    case class Library(name: String, version: String, repoUrl: String)

    def downloadAndBuildTask(
      config: Configuration,
      library: Library,
      tag: String,
    ): Def.Initialize[Task[os.Path]] = Def.task {

      val s = (config / streams).value

      implicit val jsonFormatOsPath: JsonFormat[os.Path] = BasicJsonProtocol
        .projectFormat[os.Path, File](_.toIO, os.Path(_))

      implicit val jsonFormatLibrary = BasicJsonProtocol
        .projectFormat[Library, (String, String, String)](
          l => (l.name, l.version, l.repoUrl),
          { case (name, version, repoUrl) => Library(name, version, repoUrl) },
        )

      val cached =
        simplyCached(
          downloadAndBuild
        )(
          s = s,
          tag = tag,
        )

      cached(library)

    }

    def copyLibrary(from: os.Path, to: os.Path): os.Path = {
      val target = to / from.last

      os.copy
        .over(
          from,
          target,
          createFolders = true,
        )

      target
    }

    def compileTreeSitter(
      config: Configuration
    ): Def.Initialize[Task[File]] = Def.taskDyn {
      val output = os.Path((config / resourceManaged).value)
      val version = (config / ts4sTreeSitterVersion).value

      Def.task {
        val extracted =
          downloadAndBuildTask(
            config = config,
            library = Library(
              name = "tree-sitter",
              version = version,
              repoUrl = "https://github.com/tree-sitter/tree-sitter",
            ),
            tag = "tree-sitter",
          ).value

        copyLibrary(extracted, output).toIO
      }

    }

    def compileGrammars(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.taskDyn {
      val output = os.Path((config / resourceManaged).value)

      val grammars = (config / ts4sGrammars).value.toList

      val tasks = grammars.map { grammar =>
        downloadAndBuildTask(
          config = config,
          library = Library(
            name = s"tree-sitter-${grammar.language}",
            version = grammar.version,
            repoUrl = s"https://github.com/tree-sitter/tree-sitter-${grammar.language}",
          ),
          tag = "tree-sitter-libraries",
        )
      }

      // I guess this is sbt's way of doing a traverse.
      // Can we just get an actual map/traverse?
      Def.task {
        tasks
          .joinWith(_.join)
          .value
          .map(copyLibrary(_, output).toIO)
      }
    }

  }

  override def trigger: PluginTrigger = noTrigger

  import internals._

  override def projectSettings: Seq[Setting[_]] = Seq(
    // settings
    Compile / ts4sGrammars := Nil,
    Compile / ts4sCompileCore := false,
    Compile / ts4sTreeSitterVersion := "0.22.6",

    // tasks
    Compile / ts4sBuildCore := {
      if ((Compile / ts4sCompileCore).value)
        compileTreeSitter(Compile).value :: Nil
      else
        Nil
    },
    Compile / ts4sBuildGrammars := compileGrammars(Compile).value,

    // generators
    Compile / resourceGenerators += (Compile / ts4sBuildCore).taskValue,
    Compile / resourceGenerators += (Compile / ts4sBuildGrammars).taskValue,
  )

}
