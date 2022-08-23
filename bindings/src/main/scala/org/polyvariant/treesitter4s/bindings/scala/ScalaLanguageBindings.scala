package org.polyvariant.treesitter4s.bindings.scala

import com.sun.jna.Native
import org.polyvariant.treesitter4s.bindings.LanguageRef

object ScalaLanguageBindings {

  private val LIBRARY: TreeSitterScala = Native
    .load[TreeSitterScala](
      "tree-sitter-scala",
      classOf[TreeSitterScala],
    )

  val Scala: LanguageRef = LanguageRef(LIBRARY.tree_sitter_scala())

}
