package org.polyvariant.treesitter4s.bindings

import java.nio.file.Files

object TreeSitterLanguages {

  /** Loads the native libraries required by each language grammar.
    */
  def unsafePrep(): Unit = {
    val cl = getClass().getClassLoader()

    def loadLibFromCL(name: String) = {
      val resStream = cl.getResourceAsStream(s"darwin-aarch64/$name")

      val parent = Files.createTempDirectory("treesitter4s")

      val tf = parent.resolve(name)
      Files.copy(resStream, tf)
      tf.toFile.deleteOnExit()

      System.load(tf.toString());
    }

    loadLibFromCL("libc++abi.1.dylib")
    loadLibFromCL("libc++.1.0.dylib")
  }

  // Singleton execution of unsafePrep
  val Require: Unit = unsafePrep()
}
