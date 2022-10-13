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

package org.polyvariant.treesitter4s.bindings

import java.nio.file.Files
import com.sun.jna.Platform
import java.nio.file.Path

object TreeSitterLanguages {
  val parent = Files.createTempDirectory("treesitter4s")

  /** Loads the native libraries required by each language grammar.
    */
  def unsafePrep(): Unit = {
    val cl = getClass().getClassLoader()

    if (Platform.isMac()) {
      copyLibFromCL("c++abi.1", cl)
      val p2 = copyLibFromCL("c++.1.0", cl)

      loadLibFromCL(p2)
    }
  }

  private def copyLibFromCL(name: String, classLoader: ClassLoader): Path = {
    val platformName = System.mapLibraryName(name)

    val resStream = classLoader.getResourceAsStream(
      s"${Platform.RESOURCE_PREFIX}/$platformName"
    )

    try {
      val tf = parent.resolve(platformName)
      Files.copy(resStream, tf)
      tf.toFile.deleteOnExit()
      tf
    } finally resStream.close()
  }

  private def loadLibFromCL(path: Path) =
    try System.load(path.toString())
    catch {
      case e: UnsatisfiedLinkError =>
        e.printStackTrace()
        throw new Exception("Couldn't load library", e)
    }

}
