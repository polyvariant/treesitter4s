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

package org.polyvariant.treesitter4s.bindings.scala

import com.sun.jna.Library
import com.sun.jna.Native
import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary
import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary.Language

object ScalaLanguageBindings {

  /* private  */
  trait Bindings extends Library {
    def tree_sitter_scala: TreeSitterLibrary.Language
  }

  /* private */
  val LIBRARY: Bindings = Native
    .load(
      "tree-sitter-scala",
      classOf[Bindings],
    )

  val Scala: Language = LIBRARY.tree_sitter_scala

}