package org.polyvariant.treesitter4s.bindings.scala

import org.polyvariant.treesitter4s.Language
import scalajs.js
import js.annotation.JSImport

object ScalaLanguageBindings {

  @js.native
  @js.annotation.JSImport("tree-sitter-scala", JSImport.Namespace)
  def Scala: Language = js.native

}
