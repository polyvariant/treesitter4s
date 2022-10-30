package org.polyvariant.treesitter4s.bindings.scala

import weaver._
import org.polyvariant.treesitter4s.TreeSitter

object ScalaLanguageBindingsTests extends FunSuite {

  test("Arbitrary scala file") {
    val result = TreeSitter.make(ScalaLanguageBindings.Scala).parse("object A {}").rootNode

    assert(result.nonEmpty)
  }
}
