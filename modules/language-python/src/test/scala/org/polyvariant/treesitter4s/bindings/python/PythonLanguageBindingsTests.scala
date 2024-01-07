package org.polyvariant.treesitter4s.language.python

import weaver._
import org.polyvariant.treesitter4s.TreeSitter

object PythonLanguageBindingsTests extends FunSuite {

  test("Arbitrary python file") {
    val result = TreeSitter.make(PythonLanguageBindings.Python).parse("object A {}").rootNode

    assert(result.nonEmpty)
  }
}
