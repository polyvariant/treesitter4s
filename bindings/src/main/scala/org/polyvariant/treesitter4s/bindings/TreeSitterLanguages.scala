package org.polyvariant.treesitter4s.bindings

import java.nio.file.Files
import com.sun.jna.Platform

object TreeSitterLanguages {

  /** Loads the native libraries required by each language grammar.
    */
  def unsafePrep(): Unit = {
    val cl = getClass().getClassLoader()

    def loadLibFromCL(name: String) = {
      val platformName = System.mapLibraryName(name)

      val resStream = cl.getResourceAsStream(
        s"${Platform.RESOURCE_PREFIX}/$platformName"
      )

      val parent = Files.createTempDirectory("treesitter4s")

      val tf = parent.resolve(platformName)
      Files.copy(resStream, tf)
      tf.toFile.deleteOnExit()

      System.load(tf.toString());
    }

    loadLibFromCL("c++abi.1")
    loadLibFromCL("c++.1.0")
  }

}
