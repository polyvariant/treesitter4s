package org.polyvariant.treesitter4s

import weaver.*

object TreeSitterTest extends FunSuite {
  test("Tree Sitter loads") {
    try {
      println(TreeSitter)
      success
    } catch {
      case e: ExceptionInInitializerError =>
        e.printStackTrace()
        failure("Couldn't load tree-sitter")
    }

  }
}
