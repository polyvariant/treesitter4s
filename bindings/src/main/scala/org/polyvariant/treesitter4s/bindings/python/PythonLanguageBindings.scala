package org.polyvariant.treesitter4s.bindings.python

import com.sun.jna.Native
import org.polyvariant.treesitter4s.bindings.LanguageRef

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

  val Python: LanguageRef = LanguageRef(LIBRARY.tree_sitter_python())

}
