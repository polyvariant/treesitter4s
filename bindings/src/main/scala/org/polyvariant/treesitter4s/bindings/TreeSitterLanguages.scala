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

object TreeSitterLanguages {

  /** Loads the native libraries required by each language grammar.
    */
  def unsafePrep(): Unit = {
    val cl = getClass().getClassLoader()

    if (Platform.isMac()) {
      loadLibFromCL("c++abi.1", cl)
      loadLibFromCL("c++.1.0", cl)
    }
  }

  private def loadLibFromCL(name: String, classLoader: ClassLoader) = {
    val platformName = System.mapLibraryName(name)

    val resStream = classLoader.getResourceAsStream(
      s"${Platform.RESOURCE_PREFIX}/$platformName"
    )

    val parent = Files.createTempDirectory("treesitter4s")

    val tf = parent.resolve(platformName)
    Files.copy(resStream, tf)
    tf.toFile.deleteOnExit()

    System.load(tf.toString());
  }

}
