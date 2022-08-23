package org.polyvariant.treesitter4s.bindings.scala

import com.sun.jna.Native
import org.polyvariant.treesitter4s.bindings.LanguageRef

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

  val Scala: LanguageRef = LanguageRef(LIBRARY.tree_sitter_scala())

}
