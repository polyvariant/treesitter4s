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

package org.polyvariant.treesitter4s.bindings.python

import com.sun.jna.Library
import com.sun.jna.Native
import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary
import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary.Language

object PythonLanguageBindings {

  private[python] trait Bindings extends Library {
    def tree_sitter_python: TreeSitterLibrary.Language
  }

  // dev note: making this private causes segfaults when compiling under Scala 3.
  // Perhaps this should be Java code instead (tbh that'd make the whole cross-compilation part much easier, given this is JVM-only)
  private[python] val LIBRARY: Bindings = Native.load("tree-sitter-python", classOf[Bindings])

  val Python: Language = LIBRARY.tree_sitter_python
}
