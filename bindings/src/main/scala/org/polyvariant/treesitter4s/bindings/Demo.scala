/*
 * Copyright 2022 Polyvariant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.polyvariant.treesitter4s.bindings;;

import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files

object Demo {

  def main(args: Array[String]): Unit = {

    val cl = getClass().getClassLoader()

    // this will have to be called around the initialization time of the libraries.
    // It'll have to either be a separate call (which users could wrap in a IO.blocking call)
    // or somehow be built into the API (the Scala API, if it involves cats-effect-kernel, could build it into a DSL).
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

    val ts =
      org
        .polyvariant
        .treesitter4s
        .bindings
        .TreeSitterInstance
        .LIBRARY

    System.out.println("loading scala");
    val scala = org.polyvariant.treesitter4s.bindings.scala.ScalaLanguageBindings.Scala;
    System.out.println("loaded scala");
    // Language python =
    // org.polyvariant.treesitter4s.bindings.python.PythonLanguageBindings.Python;
    System.out.println(ts.ts_language_symbol_count(scala));
    // System.out.println(ts.ts_language_symbol_count(python));

    // Parser p = ts.ts_parser_new();
    // // ts.ts_parser_set_language(p, python);
    // ts.ts_parser_set_language(p, scala);

    // byte[] bytes = "class
    // Hello\n\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    // Tree t = ts.ts_parser_parse_string(p, null, bytes, bytes.length);

    // Node n = ts.ts_tree_root_node(t);

    // System.out.println(ts.ts_node_child_count(n));
    // String s = ts.ts_node_string(n);
    // ts.ts_tree_delete(t);
    // ts.ts_parser_delete(p);
    // System.out.println(s);
  }

}
