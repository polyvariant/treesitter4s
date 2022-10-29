package org.polyvariant.treesitter4s.bindings.python

import org.polyvariant.treesitter4s.Language
import scalajs.js
import js.annotation.JSImport

object PythonLanguageBindings {

  @js.native
  @js.annotation.JSImport("tree-sitter-python", JSImport.Namespace)
  def Python: Language = js.native

}
