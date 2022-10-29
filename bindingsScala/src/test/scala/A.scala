import org.polyvariant.treesitter4s.bindings.scala.ScalaLanguageBindings

import org.polyvariant.treesitter4s.TreeSitter

object A extends weaver.FunSuite {
  test("this should run pls") {
    // ^ it does not. Even if I put this into the native sources specifically
    TreeSitter.make(ScalaLanguageBindings.Scala)

    success
  }
}
