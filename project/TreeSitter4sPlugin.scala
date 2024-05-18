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
    val compileTreeSitter = internals.compileTreeSitter _
    val compilePythonGrammar = internals.compilePythonGrammar _
  }

  private object internals {

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

    def downloadAndBuildTask(
      config: Configuration,
      name: String,
      version: String,
      repoUrl: String,
    ): Def.Initialize[Task[os.Path]] = Def.task {

      val s = (config / streams).value

      implicit val jsonFormatOsPath: JsonFormat[os.Path] = BasicJsonProtocol
        .projectFormat[os.Path, File](_.toIO, os.Path(_))

      val cached = Function.untupled(
        simplyCached((downloadAndBuild _).tupled)(
          s = s,
          tag = name,
        )
      )

      cached(name, version, repoUrl)

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

      val extracted =
        downloadAndBuildTask(
          config = config,
          name = "tree-sitter",
          version = "0.22.6",
          repoUrl = "https://github.com/tree-sitter/tree-sitter",
        ).value

      List(copyLibrary("tree-sitter", extracted, output).toIO)
    }

    def compilePythonGrammar(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
      val output = os.Path((config / resourceManaged).value)

      val extracted =
        downloadAndBuildTask(
          config = config,
          name = "tree-sitter-python",
          version = "0.21.0",
          repoUrl = "https://github.com/tree-sitter/tree-sitter-python",
        ).value

      List(copyLibrary("tree-sitter-python", extracted, output).toIO)
    }

  }

  override def trigger: PluginTrigger = noTrigger
}
