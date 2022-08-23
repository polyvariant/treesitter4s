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

package org.polyvariant.treesitter4s.bindings

import cats.implicits._
import org.polyvariant.treesitter4s.Encoding
import org.polyvariant.treesitter4s.bindings.Bindings
import weaver._
import cats.effect.IO

object BindingTests extends SimpleIOSuite {
  val ts = Bindings.instance

  private val isLinux = System.getProperty("os.name").toLowerCase().contains("linux")
  private val skipLinux = ignore("disabled on linux").whenA(isLinux)

  def parseExample(s: String) = ts.parse(s, ScalaLanguageBindings.scala, Encoding.UTF8)
  def parseExamplePython(s: String) = ts.parse(s, PythonLanguageBindings.python, Encoding.UTF8)

  pureTest("root node child count") {
    val tree = parseExample("class Hello {}")
    val rootNode = tree.rootNode

    assert.eql(rootNode.map(_.children.length), Some(1))
  }

  test("root node child type") {
    skipLinux >> {
      val tree = parseExample("class Hello {}")
      val rootNode = tree.rootNode

      assert.eql(rootNode.map(_.tpe), Some("compilation_unit")).pure[IO]
    }
  }

  test("root node child type - python") {
    skipLinux >> {
      val tree = parseExamplePython("def hello()")
      val rootNode = tree.rootNode

      assert.eql(rootNode.map(_.tpe), Some("module")).pure[IO]
    }
  }

  pureTest("root node child by index (in range)") {
    val tree = parseExample("class Hello {}")

    val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

    assert.eql(rootNode.children.lift(0).isDefined, true)
  }

  pureTest("root node child by index (out of range)") {
    val tree = parseExample("class Hello {}")
    val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

    assert.eql(rootNode.children.lift(-1).isDefined, false)
  }

  test("root node string, range") {
    skipLinux >> {
      val tree = parseExample("class Hello {}")
      val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

      val expected =
        "(compilation_unit (class_definition name: (identifier) body: (template_body)))"

      (
        assert.eql(rootNode.text, expected) &&
          assert.eql(rootNode.getStartByte, 0) &&
          assert.eql(rootNode.getEndByte, 14)
      )
        .pure[IO]
    }
  }

}
