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

import com.sun.jna.Native
import org.polyvariant.treesitter4s.TreeSitter
import org.polyvariant.treesitter4s.bindings.facade.Facade

object Bindings {

  private val libName = {
    val os = System.getProperty("os.name")

    if (os.toLowerCase().contains("mac"))
      "/libtree-sitter.dylib"
    else if (os.toLowerCase().contains("linux"))
      "/libtree-sitter.so"
    else
      sys.error(s"Unsupported system: $os")
  }

  private val LIBRARY: TreeSitterLibrary = Native
    .load[TreeSitterLibrary](
      Native.extractFromResourcePath(libName).toString(),
      classOf[TreeSitterLibrary],
    )

  val instance: TreeSitter.Aux[LanguageRef] = Facade.make(LIBRARY)

}

object ScalaLanguageBindings {

  private val libName = {
    val os = System.getProperty("os.name")

    if (os.toLowerCase().contains("mac"))
      "/tree-sitter-scala.dylib"
    else if (os.toLowerCase().contains("linux"))
      "/tree-sitter-scala.so"
    else
      sys.error(s"Unsupported system: $os")
  }

  private val LIBRARY: TreeSitterScala = Native
    .load[TreeSitterScala](
      Native.extractFromResourcePath(libName).toString(),
      classOf[TreeSitterScala],
    )

  def scala: LanguageRef = LanguageRef(LIBRARY.tree_sitter_scala())

}

object PythonLanguageBindings {

  private val libName = {
    val os = System.getProperty("os.name")

    if (os.toLowerCase().contains("mac"))
      "/tree-sitter-python.dylib"
    else if (os.toLowerCase().contains("linux"))
      "/tree-sitter-python.so"
    else
      sys.error(s"Unsupported system: $os")
  }

  private val LIBRARY: TreeSitterPython = Native
    .load[TreeSitterPython](
      Native.extractFromResourcePath(libName).toString(),
      classOf[TreeSitterPython],
    )

  def python: LanguageRef = LanguageRef(LIBRARY.tree_sitter_python())

}
