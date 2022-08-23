package org.polyvariant.treesitter4s.bindings.python

import com.sun.jna.Native
import org.polyvariant.treesitter4s.bindings.LanguageRef

object PythonLanguageBindings {

  private val LIBRARY: TreeSitterPython = Native
    .load[TreeSitterPython](
      "tree-sitter-python",
      classOf[TreeSitterPython],
    )

  val Python: LanguageRef = LanguageRef(LIBRARY.tree_sitter_python())

}
